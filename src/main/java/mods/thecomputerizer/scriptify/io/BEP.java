package mods.thecomputerizer.scriptify.io;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;

import java.util.Objects;

public class BEP {

    private final String[] elements;
    @Getter @Setter private String extra;
    @Getter @Setter private int amount;

    public BEP(String ... elements) {
        this(1,elements);
    }

    public BEP(int amount, String ... elements) {
        this.elements = elements;
        this.amount = amount;
    }
    @Override
    public String toString() {
        String ret = "null";
        if(this.amount>0) {
            ret = TextUtil.arrayToString(":",(Object[])this.elements);
            if(Objects.isNull(ret)) ret = "null";
            else {
                if(Objects.nonNull(this.extra) && !this.extra.isEmpty()) ret += this.extra;
                ret = "<"+ret+">";
                if(this.amount>1) ret += "*" + this.amount;
            }
        }
        return ret;
    }
}
