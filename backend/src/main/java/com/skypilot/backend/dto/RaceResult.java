package com.skypilot.backend.dto;

public class RaceResult {
    private final String winnerId;
    private final String winnerName;
    private final String loserId;
    private final String loserName;
    private final int winnerPoints;
    private final int loserPoints;
    private final String summary;

    public RaceResult(String winnerId, String winnerName, String loserId, String loserName,
                      int winnerPoints, int loserPoints, String summary) {
        this.winnerId = winnerId;
        this.winnerName = winnerName;
        this.loserId = loserId;
        this.loserName = loserName;
        this.winnerPoints = winnerPoints;
        this.loserPoints = loserPoints;
        this.summary = summary;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public String getLoserId() {
        return loserId;
    }

    public String getLoserName() {
        return loserName;
    }

    public int getWinnerPoints() {
        return winnerPoints;
    }

    public int getLoserPoints() {
        return loserPoints;
    }

    public String getSummary() {
        return summary;
    }
}
