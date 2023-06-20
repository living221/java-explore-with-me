package ru.practicum.rating;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@IdClass(RatingCompositePK.class)
@Table(name = "ratings")
public class Rating {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "is_positive")
    private Boolean isPositive;

    @Column(name = "initiator_id")
    private long initiatorId;
}
