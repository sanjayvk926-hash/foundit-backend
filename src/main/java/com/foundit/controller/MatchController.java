package com.foundit.controller;

import com.foundit.dto.response.BaseResponse;
import com.foundit.model.Match;
import com.foundit.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/matches")
public class MatchController {

    @Autowired
    private MatchRepository matchRepository;

    @GetMapping
    public ResponseEntity<?> getAllMatches() {
        return ResponseEntity.ok(BaseResponse.success("Fetched all matches", matchRepository.findAll()));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveMatch(@PathVariable Long id) {
        Match match = matchRepository.findById(id).orElseThrow();
        match.setStatus(Match.MatchStatus.APPROVED);
        matchRepository.save(match);
        // Trigger notification
        
        return ResponseEntity.ok(BaseResponse.success("Match approved", match));
    }
}
