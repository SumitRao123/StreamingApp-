package stream.app.payload;


import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class CustomMessage {

  private     String message;
  private  boolean success;
}
