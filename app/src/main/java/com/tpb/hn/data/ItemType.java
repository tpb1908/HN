package com.tpb.hn.data;

/**
 * Created by theo on 18/10/16.
 */

public enum ItemType {
    ALL, STORY, COMMENT, POLL, ASK, SHOW, SAVED;


    @Override
    public String toString() {
        switch(this) {
            case STORY:
                return "story";
            case POLL:
                return "poll";
            case SHOW:
                return "show_hn";
            case ASK:
                return "ask_hn";
            case COMMENT:
                return "comment";
        }
        return super.toString();
    }
}
