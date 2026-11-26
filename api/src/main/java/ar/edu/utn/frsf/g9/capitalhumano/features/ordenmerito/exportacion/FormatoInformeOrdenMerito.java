package ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.exportacion;

public enum FormatoInformeOrdenMerito {
    PDF("application/pdf", "pdf"),
    WORD("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");

    private final String contentType;
    private final String extension;

    FormatoInformeOrdenMerito(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public String contentType() { return contentType; }
    public String extension() { return extension; }
}
