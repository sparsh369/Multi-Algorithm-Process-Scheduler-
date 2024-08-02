import java.util.*;

public class SchedulingAlgorithms {

    // Global Constants
    private static final String TRACE = "trace";
    private static final String SHOW_STATISTICS = "stats";
    private static final String[] ALGORITHMS = {"", "FCFS", "RR-", "SPN", "SRT", "HRRN", "FB-1", "FB-2i", "AGING"};
    
    private static List<Tuple<String, Integer, Integer>> processes = new ArrayList<>();
    private static Map<String, Integer> processToIndex = new HashMap<>();
    private static char[][] timeline;
    private static int[] finishTime, turnAroundTime;
    private static double[] normTurn;
    private static int processCount, lastInstant;
    private static String operation;

    // Tuple class for storing process information
    private static class Tuple<T1, T2, T3> {
        T1 first;
        T2 second;
        T3 third;

        public Tuple(T1 first, T2 second, T3 third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }
    }

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

    private static boolean sortByServiceTime(Tuple<String, Integer, Integer> a, Tuple<String, Integer, Integer> b) {
        return a.third < b.third;
    }

    private static boolean sortByArrivalTime(Tuple<String, Integer, Integer> a, Tuple<String, Integer, Integer> b) {
        return a.second < b.second;
    }

    private static Comparator<Tuple<String, Double, Integer>> descendinglyByResponseRatio() {
        return (p1, p2) -> Double.compare(p2.second, p1.second);
    }

    private static void clearTimeline() {
        for (int i = 0; i < lastInstant; i++) {
            for (int j = 0; j < processCount; j++) {
                timeline[i][j] = ' ';
            }
        }
    }

    private static int getArrivalTime(Tuple<String, Integer, Integer> a) {
        return a.second;
    }

    private static int getServiceTime(Tuple<String, Integer, Integer> a) {
        return a.third;
    }

    private static double calculateResponseRatio(int waitTime, int serviceTime) {
        return (waitTime + serviceTime) * 1.0 / serviceTime;
    }

    private static void fillInWaitTime() {
        for (int i = 0; i < processCount; i++) {
            int arrivalTime = getArrivalTime(processes.get(i));
            for (int k = arrivalTime; k < finishTime[i]; k++) {
                if (timeline[k][i] != '*') {
                    timeline[k][i] = '.';
                }
            }
        }
    }

    private static void firstComeFirstServe() {
        int time = getArrivalTime(processes.get(0));
        for (int i = 0; i < processCount; i++) {
            int processIndex = i;
            int arrivalTime = getArrivalTime(processes.get(i));
            int serviceTime = getServiceTime(processes.get(i));

            finishTime[processIndex] = time + serviceTime;
            turnAroundTime[processIndex] = finishTime[processIndex] - arrivalTime;
            normTurn[processIndex] = turnAroundTime[processIndex] * 1.0 / serviceTime;

            for (int j = time; j < finishTime[processIndex]; j++) {
                timeline[j][processIndex] = '*';
            }
            for (int j = arrivalTime; j < time; j++) {
                timeline[j][processIndex] = '.';
            }
            time += serviceTime;
        }
    }

    private static void roundRobin(int originalQuantum) {
        Queue<Pair<Integer, Integer>> q = new LinkedList<>();
        int j = 0;

        if (getArrivalTime(processes.get(j)) == 0) {
            q.add(new Pair<>(j, getServiceTime(processes.get(j))));
            j++;
        }

        int currentQuantum = originalQuantum;
        for (int time = 0; time < lastInstant; time++) {
            if (!q.isEmpty()) {
                Pair<Integer, Integer> front = q.poll();
                int processIndex = front.getKey();
                int remainingServiceTime = front.getValue();
                int arrivalTime = getArrivalTime(processes.get(processIndex));
                int serviceTime = getServiceTime(processes.get(processIndex));
                currentQuantum--;

                timeline[time][processIndex] = '*';

                while (j < processCount && getArrivalTime(processes.get(j)) == time + 1) {
                    q.add(new Pair<>(j, getServiceTime(processes.get(j))));
                    j++;
                }

                if (currentQuantum == 0) {
                    if (remainingServiceTime == 0) {
                        finishTime[processIndex] = time + 1;
                        turnAroundTime[processIndex] = finishTime[processIndex] - arrivalTime;
                        normTurn[processIndex] = turnAroundTime[processIndex] * 1.0 / serviceTime;
                    } else {
                        q.add(new Pair<>(processIndex, remainingServiceTime));
                    }
                    currentQuantum = originalQuantum;
                } else if (remainingServiceTime == 0) {
                    finishTime[processIndex] = time + 1;
                    turnAroundTime[processIndex] = finishTime[processIndex] - arrivalTime;
                    normTurn[processIndex] = turnAroundTime[processIndex] * 1.0 / serviceTime;
                }
            }
            while (j < processCount && getArrivalTime(processes.get(j)) == time + 1) {
                q.add(new Pair<>(j, getServiceTime(processes.get(j))));
                j++;
            }
        }
        fillInWaitTime();
    }

    private static void shortestProcessNext() {
        PriorityQueue<Pair<Integer, Integer>> pq = new PriorityQueue<>(Comparator.comparingInt(Pair::getKey));
        int j = 0;

        for (int i = 0; i < lastInstant; i++) {
            while (j < processCount && getArrivalTime(processes.get(j)) <= i) {
                pq.add(new Pair<>(getServiceTime(processes.get(j)), j));
                j++;
            }
            if (!pq.isEmpty()) {
                Pair<Integer, Integer> top = pq.poll();
                int processIndex = top.getValue();
                int arrivalTime = getArrivalTime(processes.get(processIndex));
                int serviceTime = getServiceTime(processes.get(processIndex));
                int temp = arrivalTime;

                for (; temp < i; temp++) {
                    timeline[temp][processIndex] = '.';
                }

                temp = i;
                for (; temp < i + serviceTime; temp++) {
                    timeline[temp][processIndex] = '*';
                }

                finishTime[processIndex] = i + serviceTime;
                turnAroundTime[processIndex] = finishTime[processIndex] - arrivalTime;
                normTurn[processIndex] = turnAroundTime[processIndex] * 1.0 / serviceTime;
                i = temp - 1;
            }
        }
    }

    private static void shortestRemainingTime() {
        PriorityQueue<Pair<Integer, Integer>> pq = new PriorityQueue<>(Comparator.comparingInt(Pair::getKey));
        int j = 0;

        for (int i = 0; i < lastInstant; i++) {
            while (j < processCount && getArrivalTime(processes.get(j)) == i) {
                pq.add(new Pair<>(getServiceTime(processes.get(j)), j));
                j++;
            }
            if (!pq.isEmpty()) {
                Pair<Integer, Integer> top = pq.poll();
                int processIndex = top.getValue();
                int remainingTime = top.getKey();
                int serviceTime = getServiceTime(processes.get(processIndex));
                int arrivalTime = getArrivalTime(processes.get(processIndex));

                timeline[i][processIndex] = '*';

                if (remainingTime == 1) {
                    finishTime[processIndex] = i + 1;
                    turnAroundTime[processIndex] = finishTime[processIndex] - arrivalTime;
                    normTurn[processIndex] = turnAroundTime[processIndex] * 1.0 / serviceTime;
                } else {
                    pq.add(new Pair<>(remainingTime - 1, processIndex));
                }
            }
        }
        fillInWaitTime();
    }

    private static void highestResponseRatioNext() {
        List<Tuple<String, Double, Integer>> presentProcesses = new ArrayList<>();
        int j = 0;

        for (int currentInstant = 0; currentInstant < lastInstant; currentInstant++) {
            while (j < processCount && getArrivalTime(processes.get(j)) == currentInstant) {
                presentProcesses.add(new Tuple<>(processes.get(j).first, calculateResponseRatio(0, getServiceTime(processes.get(j))), j));
                j++;
            }
            if (!presentProcesses.isEmpty()) {
                Collections.sort(presentProcesses, descendinglyByResponseRatio());
                Tuple<String, Double, Integer> selectedProcess = presentProcesses.remove(0);
                int processIndex = selectedProcess.third;
                int arrivalTime = getArrivalTime(processes.get(processIndex));
                int serviceTime = getServiceTime(processes.get(processIndex));

                timeline[currentInstant][processIndex] = '*';
                finishTime[processIndex] = currentInstant + serviceTime;
                turnAroundTime[processIndex] = finishTime[processIndex] - arrivalTime;
                normTurn[processIndex] = turnAroundTime[processIndex] * 1.0 / serviceTime;
            }
        }
        fillInWaitTime();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Initialization and Input Handling
        processCount = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        processes.clear();
        processToIndex.clear();
        timeline = new char[1000][processCount];
        finishTime = new int[processCount];
        turnAroundTime = new int[processCount];
        normTurn = new double[processCount];
        lastInstant = 0;

        for (int i = 0; i < processCount; i++) {
            String processName = scanner.next();
            int arrivalTime = scanner.nextInt();
            int serviceTime = scanner.nextInt();
            processes.add(new Tuple<>(processName, arrivalTime, serviceTime));
            processToIndex.put(processName, i);
            lastInstant = Math.max(lastInstant, arrivalTime + serviceTime);
        }
        scanner.nextLine();  // Consume newline

        // Read and execute operations
        while (scanner.hasNextLine()) {
            operation = scanner.nextLine();
            if (operation.equals(TRACE) || operation.equals(SHOW_STATISTICS)) {
                // Depending on the command, execute the scheduling algorithms
                if (operation.contains("FCFS")) {
                    clearTimeline();
                    firstComeFirstServe();
                } else if (operation.contains("RR")) {
                    int quantum = Integer.parseInt(operation.split("-")[1]);
                    clearTimeline();
                    roundRobin(quantum);
                } else if (operation.contains("SPN")) {
                    clearTimeline();
                    shortestProcessNext();
                } else if (operation.contains("SRT")) {
                    clearTimeline();
                    shortestRemainingTime();
                } else if (operation.contains("HRRN")) {
                    clearTimeline();
                    highestResponseRatioNext();
                }

                // Display results based on operation
                if (operation.equals(SHOW_STATISTICS)) {
                    for (int i = 0; i < processCount; i++) {
                        System.out.printf("Process %d: Finish Time = %d, Turnaround Time = %d, Normalized Turnaround Time = %.2f%n",
                                i, finishTime[i], turnAroundTime[i], normTurn[i]);
                    }
                } else if (operation.equals(TRACE)) {
                    for (int i = 0; i < lastInstant; i++) {
                        for (int j = 0; j < processCount; j++) {
                            System.out.print(timeline[i][j]);
                        }
                        System.out.println();
                    }
                }
            }
        }
        scanner.close();
    }
}
