package com.ecommerce.mvc.controller;

import com.ecommerce.mvc.dao.cartItem.CartItemDAO;
import com.ecommerce.mvc.dao.order.OrderDAO;
import com.ecommerce.mvc.dao.orderItem.OrderItemDAO;
import com.ecommerce.mvc.dao.product.ProductDAO;
import com.ecommerce.mvc.dao.user.UserDAO;
import com.ecommerce.mvc.entity.*;
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

    private UserDAO userDAO;
    private ProductDAO productDAO;
    private CartItemDAO cartItemDAO;
    private OrderDAO orderDAO;
    private OrderItemDAO orderItemDAO;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public MyCommerceController(UserDAO userDAO,  ProductDAO productDAO, CartItemDAO cartItemDAO, OrderDAO orderDAO, OrderItemDAO orderItemDAO, PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.productDAO = productDAO;
        this.cartItemDAO = cartItemDAO;
        this.orderDAO = orderDAO;
        this.orderItemDAO = orderItemDAO;
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
        User theUser = userDAO.getUserByEmail(user.getEmail());
        if (theUser != null && passwordEncoder.matches(user.getPassword(), theUser.getPassword())) {
            session.setAttribute("loggedUser", theUser);

            // Check if user cart in DB is empty
            List<CartItem> userCart = cartItemDAO.getCartItems(theUser);
            if (userCart == null || userCart.isEmpty()) {
                // Migrate session cart to user cart
                List<CartItem> sessionCart = (List<CartItem>) session.getAttribute("cart");
                if (sessionCart != null && !sessionCart.isEmpty()) {
                    for (CartItem item : sessionCart) {
                        Product managedProduct = productDAO.getProductById(item.getProduct().getPid());

                        // Create a new CartItem linked to the user
                        CartItem newItem = new CartItem();
                        newItem.setUser(theUser);
                        newItem.setProduct(managedProduct);
                        newItem.setQuantity(item.getQuantity());
                        cartItemDAO.save(newItem);
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
        List<Product> products = productDAO.getAllProducts();
        model.addAttribute("products", products);

        if (!model.containsAttribute("newProduct")) {
            model.addAttribute("newProduct", new Product());
        }

        List<User> users = userDAO.getAllUsers();
        model.addAttribute("users", users);

        List<Order> orders = orderDAO.getOrders();
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
        User userToRemove = userDAO.getUserById(id);
        if (loggedUser != null && "admin".equals(loggedUser.getRole())) {
            if (userToRemove != null) {
                // Referential integrity order
                // Delete cart items
                List<CartItem> cartItems = cartItemDAO.getCartItems(userToRemove);
                if (cartItems != null && !cartItems.isEmpty()) {
                    cartItemDAO.deleteUserCartItems(userToRemove.getUserId());
                }

                // Delete order items
                List<Order> orders =  orderDAO.getUserOrders(userToRemove.getUserId());
                for  (Order order : orders) {
                    orderItemDAO.deleteOrderItems(order.getOrderId());
                }

                // Delete orders
                orderDAO.deleteUserOrders(userToRemove.getUserId());

                // Delete user
                userDAO.deleteUserById(id);
            }
            return "redirect:/ecommerce/admin#users";
        } else return "redirect:/ecommerce/login";
    }

    @PostMapping("/admin/addProduct")
    public String addProduct(@ModelAttribute("newProduct") Product product, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser != null && "admin".equals(loggedUser.getRole())) {
            productDAO.save(product);
            return "redirect:/ecommerce/admin#products";
        } else return "redirect:/ecommerce/login";
    }

    @PostMapping("/admin/removeProduct")
    public String removeProduct(@RequestParam("id") String id, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser != null && "admin".equals(loggedUser.getRole())) {
            if (productDAO.getProductById(id) != null) {
                orderItemDAO.deleteOrderItemsByProduct(id);
                productDAO.deleteProduct(id);
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
            cart = cartItemDAO.getCartItems(loggedUser);
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
        Product selectedProduct = productDAO.getProductById(id);

        Product product = productDAO.getProductById(selectedProduct.getPid());
        if (productDAO.getProductById(id) != null) {
            if (loggedUser != null) {
                User user = userDAO.getUserById(loggedUser.getUserId());
                CartItem existingCartItem = cartItemDAO.getUserCartItem(user.getUserId(), id);
                if (existingCartItem != null) {
                    existingCartItem.setQuantity(existingCartItem.getQuantity() + 1);
                    cartItemDAO.save(existingCartItem);
                } else cartItemDAO.save(new CartItem(user, product, 1));
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
            cart = cartItemDAO.getCartItems(loggedUser);
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
            CartItem cartItem = cartItemDAO.getCartItem(id);
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            cartItemDAO.save(cartItem);
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
            CartItem cartItem = cartItemDAO.getCartItem(id);
            int newQty =  cartItem.getQuantity() - 1;
            if (newQty > 0) {
                cartItem.setQuantity(cartItem.getQuantity() - 1);
                cartItemDAO.save(cartItem);
            } else cartItemDAO.deleteCartItem(cartItem.getProduct());
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
            List<CartItem> cart = cartItemDAO.getCartItems(loggedUser);
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

        List<Product> products = productDAO.getAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("cart_count", cart_count);
        return "landing-page";
    }

    @PostMapping("/order")
    public String order(@ModelAttribute Order order, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        User user = userDAO.getUserById(loggedUser.getUserId());
        List<CartItem> cart = cartItemDAO.getCartItems(user);
        int qty = cart.stream().mapToInt(CartItem::getQuantity).sum();
        float total  = (float) cart.stream().mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity()).sum();
        if (qty > 0) {
            Order newOrder = new Order(user, qty, total, order.getFullName(), order.getEmail(), order.getAddress(), order.getPaymentMethod());
            orderDAO.save(newOrder);
            for (CartItem cartItem : cart) {
                OrderItem orderItem = new OrderItem(newOrder, cartItem.getProduct(), cartItem.getQuantity());
                orderItemDAO.save(orderItem);
            }
            cartItemDAO.clearCart(user);
            return "post-order";
        } else return "redirect:/ecommerce/cart";
    }

    @GetMapping("/orders")
    public String orders(HttpSession session, Model model) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        User user = userDAO.getUserById(loggedUser.getUserId());
        model.addAttribute("orders", (List<Order>)orderDAO.getUserOrders(user.getUserId()));
        return "orders";
    }

    @PostMapping("/orders/cancel")
    public String cancel(@RequestParam String id, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        Order orderToCancel = orderDAO.getOrderById(id);
        List<OrderItem> orderItems = orderItemDAO.getOrderItems(id);

        if (orderToCancel == null || orderItems.isEmpty()) { return "redirect:/ecommerce/orders"; }

        // Clear order items
        orderItemDAO.deleteOrderItems(orderToCancel.getOrderId());

        // Cancel order
        orderDAO.deleteOrderById(orderToCancel.getOrderId());
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

        User existing = userDAO.getUserByEmail(email);
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

         userDAO.updateUser(loggedUser);

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
            if (userDAO.getUserByEmail(user.getEmail()) == null) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                userDAO.save(user);
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
