package music;

/**
 * Together represents two pieces of music playing at the same time.
 * The pieces start at the same instant, but may end at different times.
 */
public class Together implements Music {
    
    private Music m1;
    private Music m2;

    private void checkRep() {
        assert m1 != null;
        assert m2 != null;
    }
    
    /**
     * Make a Together of two pieces of music.
     * @param m1 one piece of music
     * @param m2 another piece of music
     */
    public Together(Music m1, Music m2) {
        this.m1 = m1;
        this.m2 = m2;
        checkRep();
    }
    
    /**
     * @return one of the pieces of music in this together
     */
    public Music top() {
        return m1;
    }
    
    /**
     * @return the other piece of music in this together
     */
    public Music bottom() {
        return m2;
    }
    
    /**
     * @return duration of this piece of music, the minimum time for both
     *         pieces to play
     */
    public double duration() {
        return Math.max(m1.duration(), m2.duration());
    }
    
    /**
     * Transpose the pieces in this Together.
     */
    public Music transpose(int semitonesUp) {
        return new Together(m1.transpose(semitonesUp), m2.transpose(semitonesUp));
    }
    
    /**
     * Play the pieces of this Together, together.
     */
    public void play(SequencePlayer player, double atBeat) {
        m1.play(player, atBeat);
        m2.play(player, atBeat);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        return m1.hashCode() + prime * m2.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        
        final Together other = (Together) obj;
        return m1.equals(other.m1) && m2.equals(other.m2);
    }

    @Override
    public String toString() {
        return "together(" + m1 + " |||| " + m2 + ")";
    }
}
