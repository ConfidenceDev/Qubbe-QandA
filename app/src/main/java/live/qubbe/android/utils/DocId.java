package live.qubbe.android.utils;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class DocId {
    @Exclude
    public String DocId;

    public <T extends DocId> T withId(@NonNull final  String id){
        this.DocId = id;
        return (T)this;
    }


}
