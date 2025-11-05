package ext.vnua.veterinary_beapp.modules.users.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveSessionResponse {
    private Long id;
    private String ipAddress;
    private String userAgent;
    private Instant createdAt;
    private Instant expiryDate;
    private Boolean current;
}
