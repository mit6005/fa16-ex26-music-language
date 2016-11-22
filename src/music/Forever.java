package music;

/**
 * Forever represents a piece of music playing over and over in an 
 * infinite loop. 
 */
public class Forever implements Music {
    
    private final Music m;
    
    private void checkRep() {
        assert m != null;
    }
    
    /**
     * Make a Forever.
     * @param m music to loop forever
     */
    public Forever(Music m) {
        this.m = m;
        checkRep();
    }

    /**
     * @return the piece of music that loops forever
     */
    public Music loop() {
        return m;
    }
    
    /**
     * @return duration of this forever, i.e. positive infinity
     */
    public double duration() {
        return Double.POSITIVE_INFINITY;
    }
    
    /**
     * Transpose the piece in this Forever.
     */
    public Music transpose(int semitonesUp) {
        return new Forever(m.transpose(semitonesUp));
    }
    
    /**
     * Play the piece in this Forever, forever.
     */
    public void play(SequencePlayer player, double atBeat) {
        if (m.duration() == 0) return;
        
        m.play(player, atBeat);
        
        player.addEvent((later) -> {
            play(player, atBeat + m.duration());
        }, atBeat + m.duration());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime + m.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        
        final Forever other = (Forever) obj;
        return m.equals(other.m);
    }

    @Override
    public String toString() {
        return "forever(" + m + ")";
    }
}
