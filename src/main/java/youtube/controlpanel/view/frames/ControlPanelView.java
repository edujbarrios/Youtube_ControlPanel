package youtube.controlpanel.view.frames;

import com.google.api.services.youtube.model.Video;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import youtube.controlpanel.model.resources.YouTubeEarningsCalculator;
import youtube.controlpanel.view.chart_factory.Graph;
import youtube.controlpanel.view.chart_factory.GraphFactory;
import youtube.controlpanel.view.observer.YouTubeDataObserver;

import javax.swing.*;
import java.awt.*;
import java.util.List;

// The main view class for the YouTube Control Panel
public class ControlPanelView extends JFrame implements YouTubeDataObserver {

    // Reference to the main frame
    private JFrame mainFrame;

    // Panels for displaying video details and list of videos
    private JPanel detailsPanel, videosPanel;

    // Constructor to initialize the view with the main frame
    public ControlPanelView(JFrame frame) {
        mainFrame = frame;
        prepareControlPanel();
    }

    // Initializes the detailsPanel and videosPanel
    private void prepareControlPanel() {
        detailsPanel = new JPanel();
        videosPanel = new JPanel();
    }

    // Displays the details of a selected video
    public void displayVideoDetails(Video video, String channelName) {
        detailsPanel.removeAll();
        detailsPanel.setLayout(new BorderLayout());

        // Create and display video details panel
        createVideoDetailsPanel(video, channelName);

        // Create and display charts panel
        createChartsPanel(video);

        mainFrame.add(detailsPanel, BorderLayout.CENTER);
        mainFrame.pack();
    }

    // Creates the panel displaying video details
    private void createVideoDetailsPanel(Video video, String channelName) {
        JPanel videoDetailsPanel = new JPanel(new GridLayout(6, 2));

        // Adding video details components
        videoDetailsPanel.add(createDetailPanel("Channel Name: " + channelName));
        videoDetailsPanel.add(createDetailPanel("Video Title: " + video.getSnippet().getTitle()));
        videoDetailsPanel.add(createDetailPanel("Likes: " + video.getStatistics().getLikeCount()));
        videoDetailsPanel.add(createDetailPanel("Views: " + video.getStatistics().getViewCount()));
        videoDetailsPanel.add(createDetailPanel("Comments: " + video.getStatistics().getCommentCount()));
        videoDetailsPanel.add(createDetailPanel("Estimated Earnings of the video: $" +
                String.format("%.2f", YouTubeEarningsCalculator.calculateAdjustedEarnings(video))));
        detailsPanel.add(videoDetailsPanel, BorderLayout.NORTH);
    }

    // Creates the panel displaying charts
    private void createChartsPanel(Video video) {
        JPanel chartsPanel = new JPanel(new GridLayout(1, 3));

        // Adding line chart, bar chart, and pie chart to the panel
        addChart(chartsPanel, "line", video, "Views by time ");
        addChart(chartsPanel, "bar", video, "Views by time ");
        addChart(chartsPanel, "pie", video, "Views by time ");

        detailsPanel.add(chartsPanel, BorderLayout.CENTER);
    }

    // Adds a chart to the specified panel based on the chart type
    private void addChart(JPanel panel, String chartType, Video video, String title) {
        DefaultCategoryDataset dataset = createDataset(video);
        GraphFactory graphFactory = new GraphFactory();
        Graph chartGraph = graphFactory.createGraph(chartType, dataset, title);
        JFreeChart chart = chartGraph.createChart();

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 300));
        panel.add(chartPanel);
    }

    // Creates a dataset for the charts based on video statistics
    private DefaultCategoryDataset createDataset(Video video) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String series = "Video Details";

        dataset.addValue(video.getStatistics().getLikeCount(), series, "Likes");
        dataset.addValue(video.getStatistics().getViewCount(), series, "Views");
        dataset.addValue(video.getStatistics().getCommentCount(), series, "Comments");
        dataset.addValue(YouTubeEarningsCalculator.calculateAdjustedEarnings(video), series, "Estimated Earnings");

        return dataset;
    }

    // Updates the view with the latest list of videos
    @Override
    public void update(List<Video> videos) {
        videosPanel.removeAll();
        videosPanel.setLayout(new GridLayout(5, 1));

        // Display up to 5 latest videos as buttons
        for (int count = 0; count < Math.min(videos.size(), 5); count++) {
            Video video = videos.get(count);
            String videoTitle = video.getSnippet().getTitle();
            JButton videoButton = new JButton(videoTitle);
            videoButton.addActionListener(e -> displayVideoDetails(video, video.getSnippet().getChannelTitle()));
            videosPanel.add(videoButton);
        }

        mainFrame.add(videosPanel, BorderLayout.SOUTH);
        mainFrame.pack();
    }

    // Creates a detail panel with the specified text
    private JPanel createDetailPanel(String detail) {
        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        detailPanel.add(new JLabel(detail), BorderLayout.CENTER);
        return detailPanel;
    }
}
