package music;

import static music.Pitch.OCTAVE;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MusicLanguage defines static methods for constructing and manipulating Music expressions,
 * particularly to create recursive music like rounds, canons, and fugues.
 */
public class MusicLanguage {
    
    // Prevent instantiation
    protected MusicLanguage() {}
    
    ////////////////////////////////////////////////////
    // Factory methods
    ////////////////////////////////////////////////////
    
    /**
     * Make Music from a string using a variant of abc notation
     *    (see http://www.walshaw.plus.com/abc/examples/).
     * 
     * The notation consists of whitespace-delimited symbols representing either
     * notes or rests. The vertical bar | may be used as a delimiter 
     * for measures; notes() treats it as a space.
     * Grammar:
     *     notes ::= symbol*
     *     symbol ::= . duration          // for a rest
     *              | pitch duration      // for a note
     *     pitch ::= accidental letter octave*
     *     accidental ::= empty string    // for natural,
     *                  | _               // for flat,
     *                  | ^               // for sharp
     *     letter ::= [A-G]
     *     octave ::= '                   // to raise one octave
     *              | ,                   // to lower one octave
     *     duration ::= empty string      // for 1-beat duration
     *                | /n                // for 1/n-beat duration
     *                | n                 // for n-beat duration
     *                | n/m               // for n/m-beat duration
     * 
     * Examples (assuming 4/4 common time, i.e. 4 beats per measure):
     *     C     quarter note, middle C
     *     A'2   half note, high A
     *     _D/2  eighth note, middle D flat
     * 
     * @param notes string of notes and rests in simplified abc notation given above
     * @param instr instrument to play the notes with
     */
    public static Music notes(String notes, Instrument instr) {
        Music music = rest(0);
        for (String sym : notes.split("[\\s|]+")) {
            if (!sym.isEmpty()) {
                music = concat(music, parseSymbol(sym, instr));
            }
        }
        return music;
    }
    
    /* Parse a symbol into a Note or a Rest. */
    private static Music parseSymbol(String symbol, Instrument instr) {
        Matcher m = Pattern.compile("([^/0-9]*)([0-9]+)?(/[0-9]+)?").matcher(symbol);
        if (!m.matches()) throw new IllegalArgumentException("couldn't understand " + symbol);

        String pitchSymbol = m.group(1);

        double duration = 1.0;
        if (m.group(2) != null) duration *= Integer.valueOf(m.group(2));
        if (m.group(3) != null) duration /= Integer.valueOf(m.group(3).substring(1));

        if (pitchSymbol.equals(".")) return rest(duration);
        else return note(duration, parsePitch(pitchSymbol), instr);
    }
    
    /* Parse a symbol into a Pitch. */
    private static Pitch parsePitch(String symbol) {
        if (symbol.endsWith("'")) return parsePitch(symbol.substring(0, symbol.length()-1)).transpose(OCTAVE);
        else if (symbol.endsWith(",")) return parsePitch(symbol.substring(0, symbol.length()-1)).transpose(-OCTAVE);
        else if (symbol.startsWith("^")) return parsePitch(symbol.substring(1)).transpose(1);
        else if (symbol.startsWith("_")) return parsePitch(symbol.substring(1)).transpose(-1);
        else if (symbol.length() != 1) throw new IllegalArgumentException("can't understand " + symbol);
        else return new Pitch(symbol.charAt(0));
    }
    
    /**
     * @param duration duration in beats, must be >= 0
     * @param pitch pitch to play
     * @param instrument instrument to use
     * @return pitch played by instrument for duration beats
     */
    public static Music note(double duration, Pitch pitch, Instrument instrument) {
        return new Note(duration, pitch, instrument);
    }
    
    /**
     * @param duration duration in beats, must be >= 0
     * @return rest that lasts for duration beats
     */
    public static Music rest(double duration) {
        return new Rest(duration);
    }
    
    ////////////////////////////////////////////////////
    // Functional objects
    ////////////////////////////////////////////////////
    
    public static final Function<Music,Music> IDENTITY = (m) -> m;
    
    /**
     * @return functional object f:Music->Music such that
     *         f(m) = transpose(m, semitonesUp)
     */
    public static Function<Music,Music> transposer(final int semitonesUp) {
        return (m) -> m.transpose(semitonesUp);
    }
    
    /**
     * @return function object f:Music->Music such that
     *         f(m) = delay(m, delay)
     */
    public static Function<Music,Music> delayer(final double delay) {
        return (m) -> delay(m, delay);
    }
    
    ////////////////////////////////////////////////////
    // Generic functions
    ////////////////////////////////////////////////////
    
    /**
     * @return initial combine change(initial) combine ... combine change^{n-1}(initial)
     */
    public static <T> T series(T initial, BiFunction<T,T,T> combine, Function<T,T> change, int n) {
        if (n == 1) {
            return initial;
        } else {
            return combine.apply(initial, series(change.apply(initial), combine, change, n-1));
        }
    }
    
    ////////////////////////////////////////////////////
    // Producers
    ////////////////////////////////////////////////////
    
    /**
     * @param m1 first piece of music
     * @param m2 second piece of music
     * @return m1 followed by m2
     */
    public static Music concat(Music m1, Music m2) {
        return new Concat(m1, m2);
    }
    
    /**
     * Transpose all notes upward or downward in pitch.
     * @param m music
     * @param semitonesUp semitones by which to transpose
     * @return m' such that for all notes n in m, the corresponding note n' in m'
     *         has n'.pitch() == n.pitch().transpose(semitonesUp), and m' is
     *         otherwise identical to m
     */
    public static Music transpose(Music m, int semitonesUp) {
        return m.transpose(semitonesUp);
    }
    
    /**
     * @param m1 first piece of music
     * @param m2 second piece of music
     * @return m1 played at the same time as m2
     */
    public static Music together(Music m1, Music m2) {
        return new Together(m1, m2);
    }
    
    /**
     * @param m music
     * @param delay beats delay
     * @return m delayed by delay beats
     */
    public static Music delay(Music m, double delay) {
        return concat(rest(delay), m);
    }
    
    /**
     * @param m music
     * @param f transformation
     * @param n voices, must be >= 1
     * @return n-voice counterpoint, where the i^th voice is f^{i-1}(m)
     */
    public static Music counterpoint(Music m, Function<Music,Music> f, int n) {
        return series(m, MusicLanguage::together, f, n);
    }
    
    /**
     * @param m music
     * @param delay beats delay
     * @param f transformation
     * @param n voices, must be >= 1
     * @return n-voice canon, where the i^th voice is f^{i-1}(m) (and delayed)
     */
    public static Music canon(Music m, double delay, Function<Music,Music> f, int n) {
        return counterpoint(m, f.compose(delayer(delay)), n);
    }
    
    /**
     * Make a simple n-voice round.
     * @param m music
     * @param delay delay between rounds, must be >= 0
     * @param n voices, must be >= 1
     * @return n-voice round, where each voice is identical (except for the delay)
     */
    public static Music round(Music m, double delay, int n) {
        return canon(m, delay, IDENTITY, n);
    }
    
    /**
     * Repeat a line of music n times using a single voice, transforming it each time.
     * @param m music
     * @param f transformation
     * @param n repetitions, must be >= 1
     * @return n repetitions of m, where the i^th repetition is f^{i-1}(m)
     */
    public static Music repeat(Music m, Function<Music,Music> f, int n) {
        return series(m, MusicLanguage::concat, f, n);
    }
    
    /**
     * Repeat a line of music n times using a single voice, playing it identically
     * each time.
     * @param m music
     * @param n repetitions, must be >= 1
     * @return n repetitions of m 
     */
    public static Music repeat(Music m, int n) {
        return repeat(m, IDENTITY, n);
    }
    
    /**
     * @param m music to loop forever
     * @return music that repeatedly plays m in an endless loop
     */
    public static Music forever(Music m) {
        return new Forever(m);
    }
    
    /**
     * Play two pieces of music simultaneously, both starting at the same time
     * and ending at the same time. This is done by repeating the shorter piece
     * as long as the longer piece is playing.
     * 
     * Requires either m1 or m2 runs forever, or one piece's duration is an
     * even multiple of the other piece's duration.
     * 
     * @return a piece of music that plays m1 and m2 simultaneously, ending at
     *         the same time as well
     */
    public static Music accompany(Music m1, Music m2) {
        if (m1.duration() < m2.duration()) {
            return accompany(m2, m1);
        }
        
        // now m1.duration >= m2.duration
        if (m2.duration() == Double.POSITIVE_INFINITY) {
            return together(m1, m2);
        } else if (m1.duration() == Double.POSITIVE_INFINITY) {
            return together(m1, forever(m2));
        } else {
            return together(m1, repeat(m2, (int) Math.round(m1.duration() / m2.duration())));
        }
    }
}
