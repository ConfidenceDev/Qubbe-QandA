package live.qubbe.android.models;

import java.util.Date;

import live.qubbe.android.utils.DocId;

public class MyPostsModel extends DocId {

    private String category, contentText, postId, posterId;
    private Date utc;

    public MyPostsModel(){}

    public MyPostsModel(String category, String contentText, String postId, String posterId, Date utc) {
        this.category = category;
        this.contentText = contentText;
        this.postId = postId;
        this.posterId = posterId;
        this.utc = utc;
    }

    public String getCategory() {
        return category;
    }

    public String getContentText() {
        return contentText;
    }

    public String getPostId() {
        return postId;
    }

    public String getPosterId() {
        return posterId;
    }

    public Date getUtc() {
        return utc;
    }
}
