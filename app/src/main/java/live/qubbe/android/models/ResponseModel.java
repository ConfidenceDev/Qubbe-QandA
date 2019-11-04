package live.qubbe.android.models;

import java.util.Date;

import live.qubbe.android.utils.DocId;

public class ResponseModel extends DocId {

    private String response, posterId, postId, userId;
    private Date utc;

    public ResponseModel(){}

    public ResponseModel(String response, String posterId, String postId, String userId, Date utc) {
        this.response = response;
        this.posterId = posterId;
        this.postId = postId;
        this.userId = userId;
        this.utc = utc;
    }

    public String getResponse() {
        return response;
    }

    public String getPosterId() {
        return posterId;
    }

    public String getPostId() {
        return postId;
    }

    public String getUserId() {
        return userId;
    }

    public Date getUtc() {
        return utc;
    }
}
