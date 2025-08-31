package ext.vnua.veterinary_beapp.common;

public enum RoleEnum {
    // Quản trị viên hệ thống
    ADMIN,

    // Quản lý kho - quản lý nguyên liệu, vật liệu, nhập xuất tồn
    WAREHOUSE_MANAGER,

    // Quản lý sản xuất - tạo lệnh sản xuất, quản lý dây chuyền
    PRODUCTION_MANAGER,

    // Nhân viên QC - kiểm nghiệm nguyên liệu và thành phẩm
    QC_STAFF,

    // Công nhân vận hành - thực hiện sản xuất, ghi nhận hồ sơ lô
    OPERATOR,

    // Nhân viên bảo trì - quản lý máy móc, sửa chữa thiết bị
    MAINTENANCE_STAFF,

    // Quản lý hồ sơ - công bố sản phẩm, giấy tờ pháp lý
    REGULATORY_AFFAIRS,

    // Nhân viên bán hàng - xuất kho thành phẩm
    SALES_STAFF,

    // Kế toán - quản lý giá cost, tính lợi nhuận
    ACCOUNTANT,

    // Người xem - chỉ có quyền xem báo cáo
    VIEWER
}