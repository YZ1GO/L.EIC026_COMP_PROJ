package pt.up.fe.comp2025.optimization.regAlloc;

import java.util.HashSet;
import java.util.Set;

public class LiveSet {
    public Set<String> liveIn;
    public Set<String> liveOut;

    public LiveSet() {
        this.liveIn = new HashSet<>();
        this.liveOut = new HashSet<>();
    }

    public LiveSet(Set<String> liveIn, Set<String> liveOut) {
        this.liveIn = liveIn;
        this.liveOut = liveOut;
    }

    public void cleanLiveIn() {
        this.liveIn.clear();
    }

    public void cleanLiveOut() {
        this.liveOut.clear();
    }

    public void addLiveIn(Set<String> liveIn) {
        this.liveIn.addAll(liveIn);
    }

    public void addLiveOut(Set<String> liveOut) {
        this.liveOut.addAll(liveOut);
    }

    public void removeLiveIn(Set<String> liveIn) {
        this.liveIn.removeAll(liveIn);
    }

    public void removeLiveOut(Set<String> liveOut) {
        this.liveOut.removeAll(liveOut);
    }

    public static boolean isEqual(LiveSet l1, LiveSet l2) {
        if (!l1.liveIn.equals(l2.liveIn)) return false;
        return l1.liveOut.equals(l2.liveOut);
    }
}
