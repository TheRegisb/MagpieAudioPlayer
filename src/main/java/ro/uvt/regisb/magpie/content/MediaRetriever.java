package ro.uvt.regisb.magpie.content;

import java.util.List;

/**
 * Content downloader interface.
 * Intended to connect to a remote media provider and
 * download content to local cache.
 */
public interface MediaRetriever {
    /**
     * Connect to a remote media source.
     *
     * @param address Adapter-dependant address.
     * @return Success of the connection.
     */
    boolean connect(String address);

    /**
     * Download media to a local cache.
     *
     * @param total  Media to download.
     * @param filter Adapter-dependant media filter.
     * @return List of local paths for the downloaded media.
     */
    List<String> download(int total, Object filter);

    /**
     * Disconnect from the remote source.
     */
    void disconnect();
}
