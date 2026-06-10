package idGenerator.primeNumberGenerator;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class PrimeNumberGeneratorOptimized  implements PrimeNumberGeneratorInterface{

    private final long lowerLimit;
    private final long upperLimit;
    private final List<Long> primeNumbers;

    public PrimeNumberGeneratorOptimized(long lowerLimit, long upperLimit){
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        primeNumbers = getPrimeNumbersInRange(lowerLimit, upperLimit);
    }

    public static boolean isPrime(long number){
        return LongStream.rangeClosed(2, (long) Math.sqrt(number))
        .allMatch(n -> number % n != 0);
    }

    public List<Long> getPrimeNumbersInRange(long lowerLimit, long upperLimit) {
        return LongStream.rangeClosed(lowerLimit, upperLimit)
                .filter(PrimeNumberGeneratorOptimized::isPrime).boxed()
                .collect(Collectors.toList());
    }

    @Override
    public long getRandomPrimeNumberInRange() {
        int randomIndex = (int) (Math.random() * primeNumbers.size());
        return primeNumbers.get(randomIndex);
    }
}
