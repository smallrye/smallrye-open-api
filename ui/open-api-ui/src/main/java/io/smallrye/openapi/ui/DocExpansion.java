package io.smallrye.openapi.ui;

/**
 * Controls the default expansion setting for the operations and tags.
 * It can be 'list' (expands only the tags), 'full' (expands the tags and operations) or 'none' (expands nothing).
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public enum DocExpansion {
    list,
    full,
    none
}