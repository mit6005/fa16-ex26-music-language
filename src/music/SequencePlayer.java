package music;

import java.util.function.Consumer;

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
     * Schedule an event callback at atBeat.
     * @param callback event callback function, takes the beat as its parameter
     * @param atBeat the beat at which the event occurs
     */
    public void addEvent(Consumer<Double> callback, double atBeat);
    
    /**
     * Play the scheduled music.
     */
    public void play();
    
}
