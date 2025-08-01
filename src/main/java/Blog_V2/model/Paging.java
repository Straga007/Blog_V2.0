package Blog_V2.model;

public class Paging {
    private int pageNumber;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;

    // Геттеры и сеттеры в стандартном Java-стиле
    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    // Дополнительные методы для совместимости с Thymeleaf
    public int pageNumber() {
        return pageNumber;
    }

    public int pageSize() {
        return pageSize;
    }

    public boolean hasNext() {
        return hasNext;
    }

    public boolean hasPrevious() {
        return hasPrevious;
    }
}