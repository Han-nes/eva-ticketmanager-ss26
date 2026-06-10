package idGenerator.primeNumberGenerator;

public class PrimeNumberGenerator implements PrimeNumberGeneratorInterface{
    private final long lowerLimit;
    private final long upperLimit;

    public PrimeNumberGenerator(long lowerLimit, long upperLimit){
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    public boolean isPrime(long number) {
        if (number <= 1) return false;
        for (long i = 2; i <= Math.sqrt(number); i++) {
            if (number % i == 0) return false;
        }
        return true;
    }

    public long getRandomPrimeNumberInRange(){
        long possiblePrimeNumber = -1;
        long randomTries = 0;
        while(!isPrime(possiblePrimeNumber) && randomTries < (upperLimit - lowerLimit)) {
            possiblePrimeNumber = (long) (Math.random() * (upperLimit - lowerLimit + 1)) + lowerLimit;
            randomTries++;
        }
        if(randomTries == (upperLimit - lowerLimit)) PrimeNumberGeneratorException.maximumTriesReached();
        return possiblePrimeNumber;
    }
}
