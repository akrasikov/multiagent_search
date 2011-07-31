/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model;

/**
 * Base class for search results
 * @author Anton
 */
public class SearchResult {
    private String path;
    private String resource;
    private ContentType type;
    private long hits;

    public SearchResult() {
    }

    public SearchResult(String path, String resource, ContentType type, long hits) {
        this.path = path;
        this.resource = resource;
        this.type = type;
        this.hits = hits;
    }

    public long getHits() {
        return hits;
    }

    public void setHits(long hits) {
        this.hits = hits;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public ContentType getType() {
        return type;
    }

    public void setType(ContentType type) {
        this.type = type;
    }
}
