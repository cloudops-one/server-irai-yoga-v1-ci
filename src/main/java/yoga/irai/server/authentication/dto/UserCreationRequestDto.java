package yoga.irai.server.authentication.dto;

import java.io.Serial;
import java.io.Serializable;
import lombok.*;
import yoga.irai.server.app.validator.PasswordValidate;

@Data
@ToString
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationRequestDto extends UserRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -3256246477232226567L;

    @PasswordValidate
    private String password;
}
