package live.qubbe.android.models;

import java.util.Date;

import live.qubbe.android.utils.DocId;

public class CardModel extends DocId {

    private String category, posterId, postId, contentText;
    private Date utc;

    public CardModel(){}

    public CardModel(String category, String posterId, String postId, String contentText, Date utc) {
        this.category = category;
        this.posterId = posterId;
        this.postId = postId;
        this.contentText = contentText;
        this.utc = utc;
    }

    public String getCategory() {
        return category;
    }

    public String getPosterId() {
        return posterId;
    }

    public String getPostId() {
        return postId;
    }

    public String getContentText() {
        return contentText;
    }

    public Date getUtc() {
        return utc;
    }
}
