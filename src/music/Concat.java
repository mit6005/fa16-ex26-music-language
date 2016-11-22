package music;

/**
 * Concat represents two pieces of music played one after the other.
 */
public class Concat implements Music {
    
    private final Music first;
    private final Music second;
    
    private void checkRep() {
        assert first != null;
        assert second != null;
    }
    
    /**
     * Make a Music sequence that plays m1 followed by m2.
     * @param m1 music to play first
     * @param m2 music to play second
     */
    public Concat(Music m1, Music m2) {
        this.first = m1;
        this.second = m2;
        checkRep();
    }
    
    /**
     * @return first piece in this concatenation
     */
    public Music first() {
        return first;
    }
    
    /**
     * @return second piece in this concatenation
     */
    public Music second() {
        return second;
    }
    
    /**
     * @return duration of this concatenation
     */
    public double duration() {
        return first.duration() + second.duration();
    }
    
    /**
     * Transpose the pieces in this concatenation.
     */
    public Music transpose(int semitonesUp) {
        return new Concat(first.transpose(semitonesUp), second.transpose(semitonesUp));
    }
    
    /**
     * Play this concatenation.
     */
    public void play(SequencePlayer player, double atBeat) {
        first.play(player, atBeat);
        second.play(player, atBeat + first.duration());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return first.hashCode() + prime * second.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        
        final Concat other = (Concat) obj;
        return first.equals(other.first) && second.equals(other.second);
    }

    @Override
    public String toString() {
        return first + " " + second;
    }
}
