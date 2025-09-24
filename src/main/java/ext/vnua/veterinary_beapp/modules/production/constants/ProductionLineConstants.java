package ext.vnua.veterinary_beapp.modules.production.constants;

public final class ProductionLineConstants {

    private ProductionLineConstants() {
        // Utility class
    }

    public static final class Status {
        public static final String ACTIVE = "ACTIVE";
        public static final String INACTIVE = "INACTIVE";
        public static final String UNDER_MAINTENANCE = "UNDER_MAINTENANCE";

        private Status() {
            // Utility class
        }
    }

    public static final class ValidationMessages {
        public static final String LINE_CODE_REQUIRED = "Mã dây chuyền không được để trống";
        public static final String LINE_NAME_REQUIRED = "Tên dây chuyền không được để trống";
        public static final String ID_REQUIRED = "ID không được để trống";
        public static final String LINE_CODE_EXISTS = "Mã dây chuyền đã tồn tại";
        public static final String LINE_NOT_FOUND = "Dây chuyền không tồn tại";
        public static final String INVALID_STATUS = "Trạng thái không hợp lệ";

        private ValidationMessages() {
            // Utility class
        }
    }
}