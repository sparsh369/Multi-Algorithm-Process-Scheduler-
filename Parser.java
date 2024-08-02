import java.util.*;
import java.util.stream.Collectors;

public class Parser {
    private static String operation;
    private static int lastInstant;
    private static int processCount;
    private static List<Pair<Character, Integer>> algorithms = new ArrayList<>();
    private static List<Triple<String, Integer, Integer>> processes = new ArrayList<>();
    private static List<List<Character>> timeline;
    private static Map<String, Integer> processToIndex = new HashMap<>();

    // Results
    private static List<Integer> finishTime = new ArrayList<>();
    private static List<Integer> turnAroundTime = new ArrayList<>();
    private static List<Float> normTurn = new ArrayList<>();

    // Helper class to hold pairs
    private static class Pair<K, V> {
        private final K key;
        private final V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }

    // Helper class to hold triples
    private static class Triple<K, V, U> {
        private final K key;
        private final V value;
        private final U third;

        public Triple(K key, V value, U third) {
            this.key = key;
            this.value = value;
            this.third = third;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public U getThird() {
            return third;
        }
    }

    public static void parseAlgorithms(String algorithmChunk) {
        String[] parts = algorithmChunk.split(",");
        for (String part : parts) {
            String[] tempStr = part.split("-");
            char algorithmId = tempStr[0].charAt(0);
            int quantum = tempStr.length > 1 ? Integer.parseInt(tempStr[1]) : -1;
            algorithms.add(new Pair<>(algorithmId, quantum));
        }
    }

    public static void parseProcesses(Scanner scanner) {
        for (int i = 0; i < processCount; i++) {
            String processChunk = scanner.next();
            String[] parts = processChunk.split(",");
            String processName = parts[0];
            int processArrivalTime = Integer.parseInt(parts[1]);
            int processServiceTime = Integer.parseInt(parts[2]);

            processes.add(new Triple<>(processName, processArrivalTime, processServiceTime));
            processToIndex.put(processName, i);
        }
    }

    public static void parse(Scanner scanner) {
        String algorithmChunk = scanner.next();
        operation = scanner.next();
        lastInstant = scanner.nextInt();
        processCount = scanner.nextInt();
        parseAlgorithms(algorithmChunk);
        parseProcesses(scanner);

        finishTime = new ArrayList<>(Collections.nCopies(processCount, 0));
        turnAroundTime = new ArrayList<>(Collections.nCopies(processCount, 0));
        normTurn = new ArrayList<>(Collections.nCopies(processCount, 0.0f));
        timeline = new ArrayList<>(Collections.nCopies(lastInstant, new ArrayList<>(Collections.nCopies(processCount, ' '))));
    }

    // Example method to demonstrate scheduling operations (needs to be implemented)
    private static void executeSchedulingAlgorithms() {
        // Implementation of scheduling algorithms goes here.
        // For example, call methods for FCFS, RR, SPN, etc.
        // This is just a placeholder method.
        for (Pair<Character, Integer> algorithm : algorithms) {
            switch (algorithm.getKey()) {
                case 'F':
                    // FCFS
                    break;
                case 'R':
                    // RR with quantum algorithm.getValue()
                    break;
                case 'S':
                    // SPN
                    break;
                case 'T':
                    // SRT
                    break;
                case 'H':
                    // HRRN
                    break;
                default:
                    System.out.println("Unknown algorithm: " + algorithm.getKey());
                    break;
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Parsing input data
        parse(scanner);

        // Execute scheduling algorithms
        executeSchedulingAlgorithms();

        // Display results
        System.out.println("Finish Time: " + finishTime);
        System.out.println("Turnaround Time: " + turnAroundTime);
        System.out.println("Normalized Turnaround Time: " + normTurn);
        System.out.println("Timeline:");
        for (List<Character> row : timeline) {
            System.out.println(row.stream().map(String::valueOf).collect(Collectors.joining()));
        }

        scanner.close();
    }
}
