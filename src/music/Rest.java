package music;

/**
 * Rest represents a pause in a piece of music.
 */
public class Rest implements Music {
    
    private final double duration;
    
    private void checkRep() {
        assert duration >= 0;
    }
    
    /**
     * Make a Rest that lasts for duration beats.
     * @param duration duration in beats, must be >= 0
     */
    public Rest(double duration) { 
        this.duration = duration; 
        checkRep();
    }
    
    /**
     * @return duration of this rest
     */
    public double duration() { 
        return duration; 
    }
    
    /**
     * Transpose this rest.
     */
    public Music transpose(int semitonesUp) {
        return this;
    }
    
    /**
     * Play this rest.
     */
    public void play(SequencePlayer player, double atBeat) {
        return;
    }
    
    @Override
    public int hashCode() {
        long durationBits = Double.doubleToLongBits(duration);
        return (int) (durationBits ^ (durationBits >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        
        final Rest other = (Rest) obj;
        return duration == other.duration;
    }
    
    @Override
    public String toString() {
        return "." + duration;
    }
}
