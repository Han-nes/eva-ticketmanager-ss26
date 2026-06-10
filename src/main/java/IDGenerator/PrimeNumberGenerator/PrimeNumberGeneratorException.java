package idGenerator.primeNumberGenerator;

public class PrimeNumberGeneratorException extends RuntimeException {
  public final static String maximumTries = "maximum number of tries to get a new prime number reached";

    public PrimeNumberGeneratorException(String message) {
        super(message);
    }

    public static PrimeNumberGeneratorException maximumTriesReached(){
      throw new PrimeNumberGeneratorException(maximumTries);
    }
}
