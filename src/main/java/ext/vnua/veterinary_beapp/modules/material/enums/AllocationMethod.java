package ext.vnua.veterinary_beapp.modules.material.enums;

public enum AllocationMethod {
    THEO_TRONG_LUONG,    // phân bổ theo kg/gram
    THEO_TRI_GIA,        // theo giá trị NVL/TP
    THEO_SO_LO,          // chia đều theo số batch
    THEO_GIO_CONG,       // nếu có giờ công
    TAT_CA_VAO_SAN_PHAM, // gán 100% cho 1 sản phẩm/batch cụ thể (nếu chỉ định)
    KHAC
}