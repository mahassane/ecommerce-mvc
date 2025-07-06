package com.ecommerce.mvc.controller;

import com.ecommerce.mvc.entity.*;
import com.ecommerce.mvc.service.cartItem.CartItemService;
import com.ecommerce.mvc.service.order.OrderService;
import com.ecommerce.mvc.service.orderItem.OrderItemService;
import com.ecommerce.mvc.service.product.ProductService;
import com.ecommerce.mvc.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/ecommerce")
public class MyCommerceController {

    private UserService userService;
    private ProductService  productService;
    private CartItemService cartItemService;
    private OrderService orderService;
    private OrderItemService orderItemService;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public MyCommerceController(UserService userService,  ProductService productService, CartItemService cartItemService, OrderService orderService, OrderItemService orderItemService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.productService = productService;
        this.cartItemService = cartItemService;
        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.passwordEncoder = passwordEncoder;
    }

    @ModelAttribute
    public void ensureCartExists(HttpSession session) {
        if (session.getAttribute("cart") == null) {
            session.setAttribute("cart", new ArrayList<CartItem>());
//            System.out.println("Cart: " + session.getAttribute("cart"));
        }
    }

    @GetMapping("/login")
    public String logIn(Model model, HttpSession session) {
        model.addAttribute("user", new User());
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@ModelAttribute("user") User user, HttpSession session, RedirectAttributes redirectAttributes) {
        User theUser = userService.getUserByEmail(user.getEmail());
        if (theUser != null && passwordEncoder.matches(user.getPassword(), theUser.getPassword())) {
            session.setAttribute("loggedUser", theUser);

            // Check if user cart in DB is empty
            List<CartItem> userCart = cartItemService.getCartItems(theUser);
            if (userCart == null || userCart.isEmpty()) {
                // Migrate session cart to user cart
                List<CartItem> sessionCart = (List<CartItem>) session.getAttribute("cart");
                if (sessionCart != null && !sessionCart.isEmpty()) {
                    for (CartItem item : sessionCart) {
                        Product managedProduct = productService.getProductById(item.getProduct().getPid());

                        // Create a new CartItem linked to the user
                        CartItem newItem = new CartItem();
                        newItem.setUser(theUser);
                        newItem.setProduct(managedProduct);
                        newItem.setQuantity(item.getQuantity());
                        cartItemService.save(newItem);
                    }
                    // Clear session cart after migration
                    session.setAttribute("cart", new ArrayList<CartItem>());
                }
            }

            if (theUser.getRole().equals("admin")) {
                return "redirect:/ecommerce/admin";
            } else {
                return "redirect:/ecommerce";
            }
        } else {
            redirectAttributes.addFlashAttribute("badCredentials", true);
            return "redirect:/ecommerce/login";
        }
    }


    @GetMapping("/admin")
    public String admin(Model model, HttpSession session) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);

        if (!model.containsAttribute("newProduct")) {
            model.addAttribute("newProduct", new Product());
        }

        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);

        List<Order> orders = orderService.getOrders();
        model.addAttribute("orders", orders);

        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser != null && "admin".equals(loggedUser.getRole())) {
            return "admin-page";
        }
        return "redirect:/ecommerce/login";
    }

    @PostMapping("/admin/removeUser")
    public String removeUser(@RequestParam("id") String id, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        User userToRemove = userService.getUserById(id);
        if (loggedUser != null && "admin".equals(loggedUser.getRole())) {
            if (userToRemove != null) {
                // Referential integrity order
                // Delete cart items
                List<CartItem> cartItems = cartItemService.getCartItems(userToRemove);
                if (cartItems != null && !cartItems.isEmpty()) {
                    cartItemService.deleteUserCartItems(userToRemove.getUserId());
                }

                // Delete order items
                List<Order> orders =  orderService.getUserOrders(userToRemove.getUserId());
                for  (Order order : orders) {
                    orderItemService.deleteOrderItems(order.getOrderId());
                }

                // Delete orders
                orderService.deleteUserOrders(userToRemove.getUserId());

                // Delete user
                userService.deleteUserById(id);
            }
            return "redirect:/ecommerce/admin#users";
        } else return "redirect:/ecommerce/login";
    }

    @PostMapping("/admin/addProduct")
    public String addProduct(@ModelAttribute("newProduct") Product product, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser != null && "admin".equals(loggedUser.getRole())) {
            productService.save(product);
            return "redirect:/ecommerce/admin#products";
        } else return "redirect:/ecommerce/login";
    }

    @PostMapping("/admin/removeProduct")
    public String removeProduct(@RequestParam("id") String id, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser != null && "admin".equals(loggedUser.getRole())) {
            if (productService.getProductById(id) != null) {
                orderItemService.deleteOrderItemsByProduct(id);
                productService.deleteProduct(id);
            }
            return "redirect:/ecommerce/admin#products";
        } else return "redirect:/ecommerce/login";
    }

    @GetMapping("/cart")
    public String cart(Model model, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        List<CartItem> cart;
        int cart_count = 0;
        if (loggedUser != null) {
            model.addAttribute("loggedIn", true);
            cart = cartItemService.getCartItems(loggedUser);
            if (cart != null) cart_count = cart_count = cart.stream().mapToInt(CartItem::getQuantity).sum();
        } else {
            cart = (List<CartItem>) session.getAttribute("cart");
            if (cart != null) cart_count = cart.stream().mapToInt(CartItem::getQuantity).sum();
        }
        model.addAttribute("cart", cart);
        model.addAttribute("cart_count", cart_count);
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addCartItem(@RequestParam String id, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        Product selectedProduct = productService.getProductById(id);

        Product product = productService.getProductById(selectedProduct.getPid());
        if (productService.getProductById(id) != null) {
            if (loggedUser != null) {
                User user = userService.getUserById(loggedUser.getUserId());
                CartItem existingCartItem = cartItemService.getUserCartItem(user.getUserId(), id);
                if (existingCartItem != null) {
                    existingCartItem.setQuantity(existingCartItem.getQuantity() + 1);
                    cartItemService.save(existingCartItem);
                } else cartItemService.save(new CartItem(user, product, 1));
            } else {
                ArrayList<CartItem> cart = (ArrayList<CartItem>) session.getAttribute("cart");
                for (CartItem cartItem : cart) {
                    if (cartItem.getProduct().getPid().equals(product.getPid())) {
                        cartItem.setQuantity(cartItem.getQuantity() + 1);
                        return "redirect:/ecommerce";
                    }
                }
                CartItem cartItem = new CartItem(null, product, 1);
                cartItem.setCartItemId(UUID.randomUUID().toString());
                cart.add(cartItem);
            }
        }
        return "redirect:/ecommerce";
    }

    @GetMapping("/checkout")
    public String checkout(Model model, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        List<CartItem> cart;
        float total = 0.0F;
        if (loggedUser != null) {
            cart = cartItemService.getCartItems(loggedUser);
            total  = (float) cart.stream().mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity()).sum();
            model.addAttribute("cart", cart);
            model.addAttribute("total", total);
            Order order = new Order();
            model.addAttribute("order", order);
            return "checkout";
        } else  {
            return "redirect:/ecommerce/login";
        }
    }

    @PostMapping("/cart/increment")
    public String incrementQty(@RequestParam String id, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");


        if (loggedUser != null) {
            CartItem cartItem = cartItemService.getCartItem(id);
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            cartItemService.save(cartItem);
        } else {
            ArrayList<CartItem> cart = (ArrayList<CartItem>) session.getAttribute("cart");
            if (cart != null) {
                for (CartItem item : cart) {
                    if (item.getCartItemId().equals(id)) {
                        item.setQuantity(item.getQuantity() + 1);
                        break;
                    }
                }
                session.setAttribute("cart", cart);
            }
        }
        return "redirect:/ecommerce/cart";
    }

    @PostMapping("/cart/decrement")
    public String decrementQty(@RequestParam String id, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");


        if (loggedUser != null) {
            CartItem cartItem = cartItemService.getCartItem(id);
            int newQty =  cartItem.getQuantity() - 1;
            if (newQty > 0) {
                cartItem.setQuantity(cartItem.getQuantity() - 1);
                cartItemService.save(cartItem);
            } else cartItemService.deleteCartItem(cartItem.getProduct());
        } else {
            ArrayList<CartItem> cart = (ArrayList<CartItem>) session.getAttribute("cart");
            if (cart != null) {
                for (CartItem item : cart) {
                    if (item.getCartItemId().equals(id)) {
                        item.setQuantity(item.getQuantity() - 1);
                        if (item.getQuantity() == 0) { cart.remove(item); }
                        break;
                    }
                }
                session.setAttribute("cart", cart);
            }
        }
        return "redirect:/ecommerce/cart";
    }

    @GetMapping("")
    public String home(Model model, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        int cart_count = 0;
        if (loggedUser != null && "Customer".equals(loggedUser.getRole())) {
            model.addAttribute("loggedIn", true);
            List<CartItem> cart = cartItemService.getCartItems(loggedUser);
            if (cart != null) {
                cart_count = cart.stream().mapToInt(CartItem::getQuantity).sum();
            } else {
                cart = (List<CartItem>) session.getAttribute("cart");
                cart_count = cart.stream().mapToInt(CartItem::getQuantity).sum();
            }
        } else {
            model.addAttribute("loggedIn", false);
            List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
            if (cart != null) cart_count = cart.stream().mapToInt(CartItem::getQuantity).sum();
        }

        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("cart_count", cart_count);
        return "landing-page";
    }

    @PostMapping("/order")
    public String order(@ModelAttribute Order order, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        User user = userService.getUserById(loggedUser.getUserId());
        List<CartItem> cart = cartItemService.getCartItems(user);
        int qty = cart.stream().mapToInt(CartItem::getQuantity).sum();
        float total  = (float) cart.stream().mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity()).sum();
        if (qty > 0) {
            Order newOrder = new Order(user, qty, total, order.getFullName(), order.getEmail(), order.getAddress(), order.getPaymentMethod());
            orderService.save(newOrder);
            for (CartItem cartItem : cart) {
                OrderItem orderItem = new OrderItem(newOrder, cartItem.getProduct(), cartItem.getQuantity());
                orderItemService.save(orderItem);
            }
            cartItemService.clearCart(user);
            return "post-order";
        } else return "redirect:/ecommerce/cart";
    }

    @GetMapping("/orders")
    public String orders(HttpSession session, Model model) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        User user = userService.getUserById(loggedUser.getUserId());
        model.addAttribute("orders", (List<Order>)orderService.getUserOrders(user.getUserId()));
        return "orders";
    }

    @PostMapping("/orders/cancel")
    public String cancel(@RequestParam String id, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        Order orderToCancel = orderService.getOrderById(id);
        List<OrderItem> orderItems = orderItemService.getOrderItems(id);

        if (orderToCancel == null || orderItems.isEmpty()) { return "redirect:/ecommerce/orders"; }

        // Clear order items
        orderItemService.deleteOrderItems(orderToCancel.getOrderId());

        // Cancel order
        orderService.deleteOrderById(orderToCancel.getOrderId());
        return "redirect:/ecommerce/orders";
    }

    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser != null && "Customer".equals(loggedUser.getRole())) {
            model.addAttribute("user", loggedUser);
            return "user-profile";
        } else return "redirect:/ecommerce/login";
    }

    @PostMapping("/updateProfile")
    public String updateProfile(@RequestParam String firstName, @RequestParam String lastName, @RequestParam String email, @RequestParam String password, Model model, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return "redirect:/ecommerce/login";
        }

        User existing = userService.getUserByEmail(email);
        if (existing != null && !existing.getUserId().equals(loggedUser.getUserId())) {
            model.addAttribute("user", loggedUser);
            model.addAttribute("emailError", "This email is already in use.");
            return "user-profile";
        }

        loggedUser.setFirstName(firstName);
        loggedUser.setLastName(lastName);
        loggedUser.setEmail(email);

        if (!password.isEmpty()) {
            loggedUser.setPassword(passwordEncoder.encode(password));
        }

         userService.updateUser(loggedUser);

        session.setAttribute("loggedUser", loggedUser);

        return "redirect:/ecommerce/profile";
    }


    @GetMapping("/signup")
    public String showSignUpPage(Model model) {

        User user = new User();

        model.addAttribute("user", user);

        return "signup";
    }

    @PostMapping("/signup")
    public String signUp(@ModelAttribute("user") User user, HttpSession session, RedirectAttributes redirectAttributes) {

        String emailPattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        if (!user.getEmail().equals("admin") && user.getEmail().matches(emailPattern)) {
            if (userService.getUserByEmail(user.getEmail()) == null) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                userService.save(user);
                return "redirect:/ecommerce/login";
            } else {
                redirectAttributes.addFlashAttribute("inValidEmail", "Email address already in use.");
                return  "redirect:/ecommerce/signup";
            }
        }
        else {
            redirectAttributes.addFlashAttribute("inValidEmail", "Invalid Email.");
            return "redirect:/ecommerce/signup";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
//        session.removeAttribute("loggedUser");
        session.invalidate();
        return "redirect:/ecommerce";
    }
}
