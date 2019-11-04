package live.qubbe.android.models;

import live.qubbe.android.utils.DocId;

public class ContributorsModel extends DocId {

    private String contributed;

    public ContributorsModel (){}

    public ContributorsModel(String contributed) {
        this.contributed = contributed;
    }

    public String getContributed() {
        return contributed;
    }
}
