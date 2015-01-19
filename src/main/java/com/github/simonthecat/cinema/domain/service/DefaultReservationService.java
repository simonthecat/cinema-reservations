package com.github.simonthecat.cinema.domain.service;

import com.github.simonthecat.cinema.domain.MoviePlay;
import com.github.simonthecat.cinema.domain.MoviePlayReservation;
import com.github.simonthecat.cinema.domain.MoviePlayReservationBuilder;
import com.github.simonthecat.cinema.domain.persistence.MoviePlayRepository;
import com.github.simonthecat.cinema.domain.persistence.MoviePlayReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Transactional(isolation = Isolation.SERIALIZABLE)
public class DefaultReservationService implements ReservationService {

    private MoviePlayReservationRepository moviePlayReservationRepository;

    private MoviePlayRepository moviePlayRepository;

    @Autowired
    public DefaultReservationService(MoviePlayReservationRepository moviePlayReservationRepository, MoviePlayRepository moviePlayRepository) {
        this.moviePlayReservationRepository = moviePlayReservationRepository;
        this.moviePlayRepository = moviePlayRepository;
    }

    @Override
    public MoviePlayReservation placeReservation(Long moviePlayId, String email, int seats) {
        MoviePlay moviePlay = moviePlayRepository.findOne(moviePlayId);
        int seatsTaken = moviePlayReservationRepository.findByMoviePlay(moviePlay).stream()
                .mapToInt(MoviePlayReservation::getSeatsTaken)
                .sum();
        int unreservedSeats = moviePlay.getCinemaHall().getSeats() - seatsTaken;

        if(unreservedSeats < seats) {
            throw new ReservationException("There are not enough unreserved seats");
        }

        String reservationNumber = generateReservationNumber();
        MoviePlayReservation moviePlayReservation = new MoviePlayReservationBuilder()
                .setReservationNumber(reservationNumber)
                .setEmail(email)
                .setMoviePlay(moviePlay)
                .setSeatsTaken(seats)
                .build();

        moviePlayReservationRepository.saveAndFlush(moviePlayReservation);
        return moviePlayReservation;
    }

    private String generateReservationNumber() {
        return UUID.randomUUID().toString();
    }

}