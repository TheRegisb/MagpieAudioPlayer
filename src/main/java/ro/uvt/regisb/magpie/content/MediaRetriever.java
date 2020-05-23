/*
 * Copyright 2020 Régis BERTHELOT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
