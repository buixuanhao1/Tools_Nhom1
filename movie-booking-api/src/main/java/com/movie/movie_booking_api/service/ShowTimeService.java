package com.movie.movie_booking_api.service;

import com.movie.movie_booking_api.entity.ShowTime;
import com.movie.movie_booking_api.repository.MovieRepository;
import com.movie.movie_booking_api.repository.ShowTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShowTimeService {

    private final ShowTimeRepository showTimeRepository;
    private final MovieRepository movieRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ShowTime create(ShowTime payload) {
        if (payload.getStartTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startTime required");
        }
        validatePayload(payload);
        checkConflicts(payload, null);
        
        ShowTime st = ShowTime.builder()
                .movieId(payload.getMovieId())
                .movieTmdbId(payload.getMovieTmdbId())
                .startTime(payload.getStartTime())
                .cinema(payload.getCinema())
                .room(payload.getRoom())
                .price(payload.getPrice())
                .priceVip(payload.getPriceVip() != null ? payload.getPriceVip() : (payload.getPrice() != null ? payload.getPrice() + 20000 : null))
                .movieTitle(payload.getMovieTitle())
                .format(payload.getFormat())
                .durationMinutes(payload.getDurationMinutes())
                .status(payload.getStatus() == null ? "ACTIVE" : payload.getStatus())
                .currency(payload.getCurrency() == null ? "VND" : payload.getCurrency())
                .disabled(Boolean.FALSE)
                .build();
        
        return showTimeRepository.save(st);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ShowTime update(Long id, ShowTime payload) {
        ShowTime existing = showTimeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Showtime not found"));
        
        if (payload.getStartTime() != null) {
            existing.setStartTime(payload.getStartTime());
        }
        
        validatePayload(payload);
        // Use either payload fields or existing fields if payload is partial
        if (payload.getCinema() != null) existing.setCinema(payload.getCinema());
        if (payload.getRoom() != null) existing.setRoom(payload.getRoom());
        if (payload.getPrice() != null) existing.setPrice(payload.getPrice());
        if (payload.getPriceVip() != null) existing.setPriceVip(payload.getPriceVip());
        if (payload.getMovieTitle() != null) existing.setMovieTitle(payload.getMovieTitle());
        if (payload.getFormat() != null) existing.setFormat(payload.getFormat());
        if (payload.getDurationMinutes() != null) existing.setDurationMinutes(payload.getDurationMinutes());
        if (payload.getStatus() != null) existing.setStatus(payload.getStatus());
        if (payload.getDisabled() != null) existing.setDisabled(payload.getDisabled());
        if (payload.getMovieId() != null) existing.setMovieId(payload.getMovieId());
        if (payload.getMovieTmdbId() != null) existing.setMovieTmdbId(payload.getMovieTmdbId());

        checkConflicts(existing, id);
        
        return showTimeRepository.save(existing);
    }

    private void validatePayload(ShowTime payload) {
        if (payload.getDurationMinutes() != null && payload.getDurationMinutes() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "durationMinutes must be positive");
        }
        
        // Fill movie data if missing
        if (payload.getMovieId() == null && payload.getMovieTmdbId() != null) {
            movieRepository.findByTmdbId(payload.getMovieTmdbId()).ifPresent(m -> {
                payload.setMovieId(m.getId());
                if (payload.getMovieTitle() == null || payload.getMovieTitle().isBlank()) {
                    payload.setMovieTitle(m.getTitle());
                }
            });
        } else if (payload.getMovieId() != null && (payload.getMovieTitle() == null || payload.getMovieTitle().isBlank())) {
            movieRepository.findById(payload.getMovieId()).ifPresent(m -> payload.setMovieTitle(m.getTitle()));
        }
    }

    private void checkConflicts(ShowTime st, Long excludeId) {
        if (st.getStartTime() == null || st.getDurationMinutes() == null || st.getCinema() == null || st.getRoom() == null) {
            return; // Can't check conflicts without these
        }
        LocalDateTime start = st.getStartTime();
        LocalDateTime end = start.plusMinutes(st.getDurationMinutes());
        List<ShowTime> conflicts = showTimeRepository.findConflictsExcluding(
                st.getCinema(), st.getRoom(), start, end, excludeId);
        
        if (!conflicts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Showtime conflict in room " + st.getRoom() + " at cinema " + st.getCinema());
        }
    }
}
