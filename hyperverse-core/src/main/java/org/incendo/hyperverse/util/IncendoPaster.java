//
//  Hyperverse - A minecraft world management plugin
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program. If not, see <http://www.gnu.org/licenses/>.
//

package org.incendo.hyperverse.util;

import com.google.common.base.Charsets;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.hyperverse.Hyperverse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Single class paster for the Incendo paste service
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class IncendoPaster {

    /**
     * Upload service URL
     */
    public static final String UPLOAD_PATH = "https://athion.net/ISPaster/paste/upload";
    /**
     * Valid paste applications
     */
    public static final Collection<String> VALID_APPLICATIONS =
            Arrays.asList("plotsquared", "fastasyncworldedit", "incendopermissions", "kvantum");

    private final Collection<PasteFile> files = new ArrayList<>();
    private final String pasteApplication;

    /**
     * Construct a new paster
     *
     * @param pasteApplication The application that is sending the paste
     */
    public IncendoPaster(final @NonNull String pasteApplication) {
        if (pasteApplication.isEmpty()) {
            throw new IllegalArgumentException("paste application cannot be null, nor empty");
        }
        if (!VALID_APPLICATIONS.contains(pasteApplication.toLowerCase(Locale.ENGLISH))) {
            throw new IllegalArgumentException(
                    String.format("Unknown application name: %s", pasteApplication));
        }
        this.pasteApplication = pasteApplication;
    }

    @SuppressWarnings("DefaultCharset")
    public static String readFile(final @NonNull File file) throws IOException {
        final List<String> lines;
        try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            lines = reader.lines().toList();
        }
        final StringBuilder content = new StringBuilder();
        for (int i = Math.max(0, lines.size() - 1000); i < lines.size(); i++) {
            content.append(lines.get(i)).append("\n");
        }
        return content.toString();
    }

    /**
     * Get an immutable collection containing all the files that have been added to this paster
     *
     * @return Unmodifiable collection
     */
    public @NonNull Collection<@NonNull PasteFile> getFiles() {
        return Collections.unmodifiableCollection(this.files);
    }

    /**
     * Add a file to the paster
     *
     * @param file File to paste
     */
    public void addFile(final @NonNull PasteFile file) {
        // Check to see that no duplicate files are submitted
        for (final PasteFile pasteFile : this.files) {
            if (pasteFile.fileName.equalsIgnoreCase(file.fileName())) {
                throw new IllegalArgumentException(
                        String.format("Found duplicate file with name %s", file.fileName()));
            }
        }
        this.files.add(file);
    }

    /**
     * Create a JSON string from the submitted information
     *
     * @return compiled JSON string
     */
    private @NonNull String toJsonString() {
        final StringBuilder builder = new StringBuilder("{\n");
        builder.append("\"paste_application\": \"").append(this.pasteApplication)
                .append("\",\n\"files\": \"");
        Iterator<PasteFile> fileIterator = this.files.iterator();
        while (fileIterator.hasNext()) {
            final PasteFile file = fileIterator.next();
            builder.append(file.fileName());
            if (fileIterator.hasNext()) {
                builder.append(",");
            }
        }
        builder.append("\",\n");
        fileIterator = this.files.iterator();
        while (fileIterator.hasNext()) {
            final PasteFile file = fileIterator.next();
            builder.append("\"file-").append(file.fileName()).append("\": \"")
                    .append(file.content().replaceAll("\"", "\\\\\"")).append("\"");
            if (fileIterator.hasNext()) {
                builder.append(",\n");
            }
        }
        builder.append("\n}");
        return builder.toString();
    }

    /**
     * Upload the paste and return the status message
     *
     * @return Status message
     * @throws Throwable any and all exceptions
     */
    public @NonNull String upload() throws Throwable {
        final URL url = new URL(UPLOAD_PATH);
        final URLConnection connection = url.openConnection();
        final HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setDoOutput(true);
        final byte[] content = this.toJsonString().getBytes(Charsets.UTF_8);
        httpURLConnection.setFixedLengthStreamingMode(content.length);
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setRequestProperty("Accept", "*/*");
        httpURLConnection.connect();
        try (final OutputStream stream = httpURLConnection.getOutputStream()) {
            stream.write(content);
        }
        if (!httpURLConnection.getResponseMessage().contains("OK")) {
            if (httpURLConnection.getResponseCode() == 413) {
                final long size = content.length;
                Hyperverse.getPlugin(Hyperverse.class).getLogger()
                        .warning(String.format("Paste Too Big > Size: %dMB", size / 1_000_000));
            }
            throw new IllegalStateException(String
                    .format(
                            "Server returned status: %d %s", httpURLConnection.getResponseCode(),
                            httpURLConnection.getResponseMessage()
                    ));
        }
        final StringBuilder input = new StringBuilder();
        try (final BufferedReader inputStream = new BufferedReader(
                new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = inputStream.readLine()) != null) {
                input.append(line).append("\n");
            }
        }
        return input.toString();
    }


    /**
     * Simple class that represents a paste file
     * Construct a new paste file
     *
     * @param fileName File name, cannot be empty, nor null
     * @param content  File content, cannot be empty, nor null
     */
    public record PasteFile(@NonNull String fileName, @NonNull String content) {
        public PasteFile {
            if (fileName.isEmpty()) {
                throw new IllegalArgumentException("file name cannot be null, nor empty");
            }
            if (content.isEmpty()) {
                throw new IllegalArgumentException("content cannot be null, nor empty");
            }
        }

        /**
         * Get the file name
         *
         * @return File name
         */
        @Override
        public @NonNull String fileName() {
            return this.fileName;
        }

        /**
         * Get the file content as a single string
         *
         * @return File content
         */
        @Override
        public @NonNull String content() {
            return this.content;
        }

    }

}
