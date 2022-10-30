package smart.photos.view;

public enum InvalidType {
    UNKNOWN,
    IMAGE_DELETED,
    LINK_EXPIRED;

    public static InvalidType fromInt(int x) {
        final InvalidType[] allTypes = InvalidType.values();
        try {
            return allTypes[x];
        } catch (IndexOutOfBoundsException e) {
            return InvalidType.UNKNOWN;
        }
    }
}
