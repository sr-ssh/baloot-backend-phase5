package ir.ac.ut.ie.Entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.ac.ut.ie.Exceptions.InvalidCommand;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Comment {
    private Integer commentId;
    private String userEmail;
    private Integer commodityId;
    private String text;
    private Date date;
    private int like;
    private int dislike;
    private Map<String, Integer> votes;

    public Comment() {
    }

    public Comment(Integer comment_id) {
        commentId = comment_id;
        like = 0;
        dislike = 0;
        date = new Date();
        votes = new HashMap<>();
    }

    public void initialValues(Integer commentId) {
        this.commentId = commentId;
        like = 0;
        dislike = 0;
        votes = new HashMap<>();
    }

    public void addVote(Vote vote) {
        if (votes.containsKey(vote.getUsername())) {
            if (votes.get(vote.getUsername()) == 1)
                like -= 1;
            if (votes.get(vote.getUsername()) == -1)
                dislike -= 1;
        }
        updateLikeDislike(vote);
        votes.put(vote.getUsername(), vote.getVote());
    }

    private void updateLikeDislike(Vote vote) {
        if (vote.getVote() == 1)
            like += 1;
        if (vote.getVote() == -1)
            dislike += 1;
    }

    public ObjectNode getInformation(ObjectMapper mapper) {
        ObjectNode commentMapper = mapper.createObjectNode();
        commentMapper.put("commentId", commentId);
        commentMapper.put("userEmail", userEmail);
        commentMapper.put("text", text);
        commentMapper.put("like", like);
        commentMapper.put("dislike", dislike);
        return commentMapper;
    }

    public void checkForInvalidCommand() throws InvalidCommand {
        if (userEmail == null || commodityId == null || text == null || date == null)
            throw new InvalidCommand();
    }

    public int getCommentId() {
        return commentId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Integer getCommodityId() {
        return commodityId;
    }

    public void setCommodityId(Integer commodityId) {
        this.commodityId = commodityId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public int getLike() {
        return like;
    }

    public int getDislike() {
        return dislike;
    }

    public Map<String, Integer> getVotes() {
        return votes;
    }
}
