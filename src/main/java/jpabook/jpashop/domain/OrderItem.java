package jpabook.jpashop.domain;

import jakarta.persistence.*;
import jpabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice;

    private int count;

    /**
     * 주문상품 생성
     * @param item
     * @param orderPrice
     * @param count
     * @return
     */
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        item.removeStock(count);
        return orderItem;
    }

    /**
     * 주문상품 취소
     */
    public void cancel() {
        getItem().addStock(count);
    }
    /*
        우선 객체 외부에서는 당연히 필드에 직접 접근하면 안되겠지만, 객체 내부에서는 필드에 직접 접근해도 아무 문제가 없습니다.
        번거롭게 getXxx를 호출하는 것 보다는 필드를 직접 호출하는 것이 코드도 더 깔끔하고요.
        그래서 저도 필드에 직접 접근하는 방법을 주로 사용합니다.
        저기서 count 대신에 getCount()를 호출한 것은 사실 아무 의미가 없습니다. 그냥 손이 가다가 보니...
        그런데! 사실은 객체 내부에서 필드에 직접 접근하는가, 아니면 getter를 통해서 접근하는가가 JPA 프록시를 많이 다루게 되면 중요해집니다.
        일반적으로 이런 상황을 겪을일은 거의 없지만, 조회한 엔티티가 프록시 객체인 경우 필드에 직접 접근하면 원본 객체를 가져오지 못하고 프록시 객체의 필드에 직접 접근해버리게 됩니다.
        이게 일반적인 상황에는 문제가 없는데, equals, hashcode를 JPA 프록시 객체로 구현할 때 문제가 될 수 있습니다.
        프록시 객체의 equals를 호출했는데 거기서 필드에 직접 접근하면, 프록시 객체는 필드에 값이 없으므로 항상 null이 반환됩니다.
        그래서 JPA 엔티티에서 equals, hashcode를 구현할 때는 getter를 내부에서 사용해야 합니다.
        방금 말씀드린 내용은 JPA 고급이어서 조금 어려울 수 있는데, 더 자세한 내용은 JPA 책 15.3.3 프록시 동등성 비교에 잘 정리되어 있습니다^^
        감사합니다.
     */

    /**
     * 주문상품 전체 가격 조회
     * @return
     */
    public int getTotalPrice() {
        return getOrderPrice() * getCount();
    }
}
