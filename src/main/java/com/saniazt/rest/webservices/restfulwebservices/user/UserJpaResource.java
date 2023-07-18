package com.saniazt.rest.webservices.restfulwebservices.user;

import com.saniazt.rest.webservices.restfulwebservices.jpa.PostRepository;
import com.saniazt.rest.webservices.restfulwebservices.jpa.UserRepository;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
public class UserJpaResource {
    private final UserRepository repository;
    private final PostRepository postRepository;
    public UserJpaResource(UserRepository userRepository, PostRepository postRepository) {
        this.repository = userRepository;
        this.postRepository = postRepository;
    }

    @GetMapping("/jpa/users")
    public List<User> retrieveAllUsers(){
        return repository.findAll();
    }


    @GetMapping("/jpa/users/{id}")
    public EntityModel<User> retrieveUser(@PathVariable int id){
        Optional<User> user = repository.findById(id);
        if(user.isEmpty()) throw new UserNotFoundException("id"+id);
        EntityModel<User> entityModel = EntityModel.of(user.get());
        WebMvcLinkBuilder linkBuilder = WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).retrieveAllUsers());
        entityModel.add(linkBuilder.withRel("all-users"));
        return entityModel;
    }


    @PostMapping("/jpa/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user){
        User savedUser = repository.save(user);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedUser.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }
    @DeleteMapping ("/jpa/users/{id}")
    public void deleteUser(@PathVariable int id){
        repository.deleteById(id);
    }
    @GetMapping ("/jpa/users/{id}/posts")
    public List<Post> retrievePostsForUser(@PathVariable int id){
        Optional<User> user = repository.findById(id);
        if(user.isEmpty()) throw new UserNotFoundException("id"+id);
        return user.get().getPosts();
    }
    @PostMapping  ("/jpa/users/{id}/posts")
    public ResponseEntity<Object> createPostForUser(@PathVariable int id, @Valid @RequestBody Post post){
        Optional<User> user = repository.findById(id);
        if(user.isEmpty()) throw new UserNotFoundException("id"+id);
        post.setUser(user.get());
        Post savedPost = postRepository.save(post);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedPost.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }
    @GetMapping("/jpa/users/{userId}/posts/{postId}")
    public EntityModel<Post> retrievePostForUser(@PathVariable int userId, @PathVariable int postId) {
        // First, check if the user exists
        Optional<User> user = repository.findById(userId);
        if (user.isEmpty()) {
            throw new UserNotFoundException("id" + userId);
        }

        // Next, check if the post exists for the given user
        Post post = findPostForUser(user.get(), postId);
        if (post == null) {
            throw new UserNotFoundException("Post not found for id" + postId);
        }

        // Create an EntityModel to wrap the Post and add HATEOAS links
        EntityModel<Post> entityModel = EntityModel.of(post);
        WebMvcLinkBuilder userLink = WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(this.getClass()).retrieveUser(userId));
        entityModel.add(userLink.withRel("user"));
        // Add more links if needed

        return entityModel;
    }

    // Helper method to find a post for a given user by post ID
    private Post findPostForUser(User user, int postId) {
        for (Post post : user.getPosts()) {
            if (post.getId() == postId) {
                return post;
            }
        }
        return null;
    }
}
