package ir.ac.ut.ie.Entities;

import ir.ac.ut.ie.Exceptions.InvalidVoteValue;

public class Vote {
    private final String username;
    private final Integer commentId;
    private final Integer vote;

    public Vote(String username, Integer commentId, Integer vote) {
        this.username = username;
        this.commentId = commentId;
        this.vote = vote;
    }

    public void hasError() throws Exception {
        if (vote == null)
            throw new InvalidVoteValue();
        if (!(vote == 1 || vote == -1 || vote == 0))
            throw new InvalidVoteValue();
    }

    public String getUsername() {
        return username;
    }
    public Integer getCommentId() {
        return commentId;
    }
    public int getVote() {
        return vote;
    }
}
