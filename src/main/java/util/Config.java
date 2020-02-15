package util;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class Config {
    private final static String CONFIG_PATH = "config";

    public final static String PROPERTY_HASH_SLOTS = "hash_slots";
    private final static String PROPERTY_NODES = "nodes";
    private final static String PROPERTY_START_PORT = "start_port";
    private final static String PROPERTY_PORT_RANGE = "port_range";
    private final static String PROPERTY_NUMBER_OF_REPLICAS = "number_of_replicas";
    public final static String PROPERTY_INIT_NUMBER_OF_ACTIVE_NODES = "init_number_of_active_nodes";
    private final static String PROPERTY_VIRTUAL_PHYSICAL_RATIO = "virtual_physical_ratio";
    private final static String PROPERTY_NUMBER_OF_PLACEMENT_GROUPS = "number_of_placement_groups";
    private final static String PROPERTY_INITIAL_WEIGHT = "initial_weight";
    private final static String PROPERTY_NUMBER_OF_RUSH_LEVEL = "number_of_rush_level";
    private final static String PROPERTY_CLUSTER_CAPACITY = "cluster_capacity";
    private final static String PROPERTY_RUSH_LEVEL_NAMES = "rush_level_names";
    private final static String PROPERTY_ENABLE_CROSS_CLUSTER_LOAD_BALANCING = "enable_cross_clusters_load_balancing";
    private final static String PROPERTY_SEEDS = "seeds";
    private final static String PROPERTY_CLUSTER_NAME = "cluster_name";
    public final static String PROPERTY_MODE = "mode";
    private final static String PROPERTY_LOG_SERVER = "log_server";
    private final static String PROPERTY_LOG_MODE = "log_mode";
    public final static String PROPERTY_SCHEME = "scheme";
    private final static String PROPERTY_NETWORK_SPEED = "network_speed";
    private final static String PROPERTY_RATIO_READ_WRITE = "read_write_ratio";
    private final static String PROPERTY_RATIO_LOAD_BALANCING = "load_balancing_ratio";
    private final static String PROPERTY_INTER_ARRIVAL_RATE_READ_WRITE = "read_write_inter_arrival_rate";
    private final static String PROPERTY_INTER_ARRIVAL_RATE_LOAD_BALANCING = "load_balancing_inter_arrival_rate";
    private final static String PROPERTY_REQUEST_CLIENT = "request_client";
    private final static String PROPERTY_REQUEST_DISTRIBUTION = "request_distribution";
    private final static String PROPERTY_REQUEST_ZIPF_ALPHA = "alpha";
    private final static String PROPERTY_REQUEST_EXP_LAMDA = "lamda";
    private final static String PROPERTY_REQUEST_NUMBER_OF_THREADS = "number_threads";
    private final static String PROPERTY_READ_OVERHEAD = "read_overhead";
    private final static String PROPERTY_WRITE_OVERHEAD = "write_overhead";
    private final static String PROPERTY_LOAD_INFO_REPORT_INTERVAL = "load_info_report_interval";
    private final static String PROPERTY_STAT_SERVER = "stat_server";
    private final static String PROPERTY_ENABLE_STAT_SERVER = "enable_stat_server";
    private final static String PROPERTY_DATA_SERVER = "data_server";
    private final static String PROPERTY_LB_UPPER_BOUND = "lb_upper_bound";
    private final static String PROPERTY_LB_LOWER_BOUND = "lb_lower_bound";
    private final static String PROPERTY_LOG_FILTER = "log_filter";
    private final static String PROPERTY_GOSSIP_STRATEGY = "gossip_strategy";
    private final static String PROPERTY_NUMBER_OF_GOSSIP_NEIGHBORS = "number_of_gossip_neighbor";
    private final static String PROPERTY_TABLE_DELTA_SIZE = "table_delta_size";
    public final static String PROPERTY_TRIAL_TAG = "trial_tag";
    public final static String PROPERTY_NUMBER_OF_REQUESTS = "num_of_requests";
    public final static String PROPERTY_DELAY_TO_STOP_ALL = "delay_to_stop_all";
    private final static String PROPERTY_RING_LB_ALGORITHM = "ring_lb_algorithm";
    private final static String PROPERTY_MAX_LOOK_FORWARD = "max_look_forward";

    public final static String STATUS_ACTIVE = "active";
    public final static String STATUS_INACTIVE = "inactive";
    public final static String MODE_DISTRIBUTED = "distributed";
    public final static String MODE_CENTRIALIZED = "centralized";
    public final static String LOG_MODE_SCREEN = "screen";
    public final static String LOG_MODE_SERVER = "server";
    public final static String LOG_MODE_FILE = "file";
    public final static String LOG_MODE_OFF = "off";
    public final static String SCHEME_RING = "ring";
    public final static String SCHEME_ELASTIC = "elastic";
    public final static String SCHEME_CEPH = "ceph";
    public final static int RATIO_KEY_READ = 0;
    public final static int RATIO_KEY_WRITE = 1;
    public final static int RATIO_KEY_ADD = 0;
    public final static int RATIO_KEY_REMOVE = 1;
    public final static int RATIO_KEY_LOAD_BALANCING = 2;
    public final static String REQUEST_DISTRIBUTION_UNIFORM = "uniform";
    public final static String REQUEST_DISTRIBUTION_ZIPF = "zipf";
    public final static String REQUEST_DISTRIBUTION_EXP = "exp";
    public final static String GOSSIP_STRATEGY_SIMPLE = "simple";
    public final static String GOSSIP_STRATEGY_NEIGHBOR = "neighbor";
    public final static String RING_LB_ALGO_NORMAL = "normal";
    public final static String RING_LB_ALGO_VNODE = "vnode";
    public final static String RING_LB_ALGO_FORWARD = "forward";

    private static volatile Config instance = null;

    private ResourceBundle rb;

    private int numberOfHashSlots;

    private int defaultNumberOfHashSlots;

    private int numberOfReplicas;

    private int numberOfPlacementGroups;

    private String scheme;

    private float initialWeight;

    private long networkSpeed;

    private boolean standalone;

    private String address;

    private int port;

    public Config() {
        rb = ResourcesLoader.getBundle(CONFIG_PATH);
        numberOfReplicas = Integer.valueOf(rb.getString(PROPERTY_NUMBER_OF_REPLICAS));
        numberOfHashSlots = Integer.valueOf(rb.getString(PROPERTY_HASH_SLOTS));
        numberOfPlacementGroups = Integer.valueOf(rb.getString(PROPERTY_NUMBER_OF_PLACEMENT_GROUPS));
        defaultNumberOfHashSlots = numberOfHashSlots;
        scheme = rb.getString(PROPERTY_SCHEME);
        initialWeight = Float.valueOf(rb.getString(PROPERTY_INITIAL_WEIGHT));
        networkSpeed = Utils.parseLong(rb.getString(PROPERTY_NETWORK_SPEED));
        standalone = false;
    }

    public static Config getInstance() {
        if (instance == null) {
            synchronized(Config.class) {
                if (instance == null) {
                    instance = new Config();
                }
            }
        }

        return instance;
    }

    public static void with(String address, int port) {
        Config.getInstance().address = address;
        Config.getInstance().port = port;
    }

    public static void deleteInstance() {
        instance = null;
    }

    public static String getConfigPath() {
        return CONFIG_PATH;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public int getNumberOfHashSlots() {
        return numberOfHashSlots;
    }

    public int getDefaultNumberOfHashSlots() {
        return defaultNumberOfHashSlots;
    }

    public String[] getNodes() {
        return rb.getString(PROPERTY_NODES).split(",");
    }

    public int getStartPort() {
        return Integer.valueOf(rb.getString(PROPERTY_START_PORT));
    }

    public int getPortRange() {
        return Integer.valueOf(rb.getString(PROPERTY_PORT_RANGE));
    }

    public int getNumberOfReplicas() {
        return numberOfReplicas;
    }

    public int getInitNumberOfActiveNodes() {
        return Integer.valueOf(rb.getString(PROPERTY_INIT_NUMBER_OF_ACTIVE_NODES));
    }

    public int getVirtualPhysicalRatio() {
        return Integer.valueOf(rb.getString(PROPERTY_VIRTUAL_PHYSICAL_RATIO));
    }

    public int getNumberOfPlacementGroups() {
        return numberOfPlacementGroups;
    }

    public float getInitialWeight() {
        return initialWeight;
    }

    public int getNumberOfRushLevel() {
        return Integer.valueOf(rb.getString(PROPERTY_NUMBER_OF_RUSH_LEVEL));
    }

    public int getClusterCapacity() {
        return Integer.valueOf(rb.getString(PROPERTY_CLUSTER_CAPACITY));
    }

    public String[] getRushLevelNames() {
        return rb.getString(PROPERTY_RUSH_LEVEL_NAMES).split(",");
    }

    public boolean enableCrossClusterLoadBalancing() {
        return Boolean.valueOf(rb.getString(PROPERTY_ENABLE_CROSS_CLUSTER_LOAD_BALANCING));
    }

    public List<String> getSeeds() {
        return Arrays.asList(rb.getString(PROPERTY_SEEDS).split(","));
    }

    public String getClusterName() {
        return rb.getString(PROPERTY_CLUSTER_NAME);
    }

    public String getMode() {
        return rb.getString(PROPERTY_MODE);
    }

    public String getLogServer() {
        return rb.getString(PROPERTY_LOG_SERVER);
    }

    public String getLogMode() {
        return rb.getString(PROPERTY_LOG_MODE);
    }

    public String getScheme() {
        return scheme;
    }

    public long getNetworkSpeed() {
        return networkSpeed;
    }

    public String getRequestClient() {
        return rb.getString(PROPERTY_REQUEST_CLIENT);
    }

    public double[] getReadWriteRatio() {
        String[] ratio = rb.getString(PROPERTY_RATIO_READ_WRITE).split(",");
        return Arrays.stream(ratio)
                .mapToDouble(Double::parseDouble)
                .toArray();
    }

    public double[] getLoadBalancingRatio() {
        String[] ratio = rb.getString(PROPERTY_RATIO_LOAD_BALANCING).split(",");
        return Arrays.stream(ratio)
                .mapToDouble(Double::parseDouble)
                .toArray();
    }

    public double getReadWriteInterArrivalRate() {
        return Double.valueOf(rb.getString(PROPERTY_INTER_ARRIVAL_RATE_READ_WRITE));
    }

    public double getLoadBalancingInterArrivalRate() {
        return Double.valueOf(rb.getString(PROPERTY_INTER_ARRIVAL_RATE_LOAD_BALANCING));
    }

    public String getRequestDistribution() {
        return rb.getString(PROPERTY_REQUEST_DISTRIBUTION);
    }

    public double getZipfAlpha() {
        return Double.valueOf(rb.getString(PROPERTY_REQUEST_ZIPF_ALPHA));
    }

    public double getExpLamda() {
        return Double.valueOf(rb.getString(PROPERTY_REQUEST_EXP_LAMDA));
    }

    public int getNumberOfThreads() {
        return Integer.valueOf(rb.getString(PROPERTY_REQUEST_NUMBER_OF_THREADS));
    }

    public float getReadOverhead() {
        return Float.valueOf(rb.getString(PROPERTY_READ_OVERHEAD));
    }

    public float getWriteOverhead() {
        return Float.valueOf(rb.getString(PROPERTY_WRITE_OVERHEAD));
    }

    public long getLoadInfoReportInterval() {
        return Long.valueOf(rb.getString(PROPERTY_LOAD_INFO_REPORT_INTERVAL));
    }

    public String getStatServer() {
        return rb.getString(PROPERTY_STAT_SERVER);
    }

    public boolean isEnableStatServer() {
        return Boolean.valueOf(rb.getString(PROPERTY_ENABLE_STAT_SERVER));
    }

    public String getDataServer() {
        return rb.getString(PROPERTY_DATA_SERVER);
    }

    public long getLoadBalancingUpperBound() {
        return Utils.parseLong(rb.getString(PROPERTY_LB_UPPER_BOUND));
    }

    public long getLoadBalancingLowerBound() {
        return Utils.parseLong(rb.getString(PROPERTY_LB_LOWER_BOUND));
    }

    public List<String> getLogFilter() {
        return Arrays.asList(rb.getString(PROPERTY_LOG_FILTER).split(","));
    }

    public String getGossipStrategy() {
        return rb.getString(PROPERTY_GOSSIP_STRATEGY);
    }

    public int getTableDeltaSize() {
        return Integer.valueOf(rb.getString(PROPERTY_TABLE_DELTA_SIZE));
    }

    public int getNumberOfGossipNeighbors() {
        return Integer.valueOf(rb.getString(PROPERTY_NUMBER_OF_GOSSIP_NEIGHBORS));
    }

    public boolean isStandalone() {
        return standalone;
    }

    public void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }

    public int getTrialTag() {
        return Integer.valueOf(rb.getString(PROPERTY_TRIAL_TAG));
    }

    public int getNumberOfRequests() {
        return Integer.valueOf(rb.getString(PROPERTY_NUMBER_OF_REQUESTS));
    }

    public int getDelayToStopAll() {
        return Integer.valueOf(rb.getString(PROPERTY_DELAY_TO_STOP_ALL));
    }

    public String getRingLbAlgorithm() {
        return rb.getString(PROPERTY_RING_LB_ALGORITHM);
    }

    public int getMaxLookForward() {
        return Integer.valueOf(rb.getString(PROPERTY_MAX_LOOK_FORWARD));
    }
}
