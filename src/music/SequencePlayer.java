package music;

/**
 * Schedules and plays a sequence of notes at given times.
 */
public interface SequencePlayer {
    
    /**
     * Schedule a note to be played starting at startBeat for the duration numBeats.
     * @param instr instrument for the note
     * @param pitch pitch value of the note
     * @param startBeat the starting beat
     * @param numBeats the number of beats the note is played
     */
    public void addNote(Instrument instr, Pitch pitch, double startBeat, double numBeats);
    
    /**
     * Play the scheduled music.
     */
    public void play();
    
}
