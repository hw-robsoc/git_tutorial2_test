import java.util.*;
import java.util.function.Supplier;

/* ===========================
   Core Behavioral Abstractions
   =========================== */

interface Audible {
    void emit();
}

interface AnimalContract {
    Optional<Audible> resolveSoundEmitter();
}

/* ===========================
   Enum-Based Sound Registry
   =========================== */

enum AnimalSoundRegistry {
    GENERIC(() -> System.out.println("The animal makes a sound")),
    PIG(() -> System.out.println("The pig says: oink oink")),
    DOG(() -> System.out.println("The dog says: bow wow")),
    CAT(() -> System.out.println("The dog says: meow meow"));

    private final Audible audible;

    AnimalSoundRegistry(Audible audible) {
        this.audible = audible;
    }

    public Audible getAudible() {
        return audible;
    }
}

/* ===========================
   Abstract Animal With Layers
   =========================== */

abstract class Animal implements AnimalContract {

    protected abstract AnimalSoundRegistry getRegistryKey();

    @Override
    public Optional<Audible> resolveSoundEmitter() {
        return Optional.ofNullable(getRegistryKey())
                       .map(AnimalSoundRegistry::getAudible);
    }

    public final void animalSound() {
        Optional<Audible> optionalAudible = resolveSoundEmitter();

        if (optionalAudible.isPresent()) {
            optionalAudible.get().emit();
        } else {
            new IllegalStateException("No sound available").printStackTrace();
        }
    }

}

/* ===========================
   Concrete Animal Implementations
   =========================== */

class GenericAnimal extends Animal {
    @Override
    protected AnimalSoundRegistry getRegistryKey() {
        return AnimalSoundRegistry.GENERIC;
    }
}

class Pig extends Animal {
    @Override
    protected AnimalSoundRegistry getRegistryKey() {
        return AnimalSoundRegistry.PIG;
    }
}

class Dog extends Animal {
    @Override
    protected AnimalSoundRegistry getRegistryKey() {
        return AnimalSoundRegistry.DOG;
    }
}

class Cat extends Animal {
    @Override
    protected AnimalSoundRegistry getRegistryKey() {
        return AnimalSoundRegistry.CAT;
    }
}

/* ===========================
   Animal Factory With Excessive Indirection
   =========================== */

class AnimalFactory {

    private static final Map<String, Supplier<Animal>> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put("animal", GenericAnimal::new);
        REGISTRY.put("pig", Pig::new);
        REGISTRY.put("dog", Dog::new);
        REGISTRY.put("cat", Cat::new);
    }

    public static Animal create(String key) {
        return Optional.ofNullable(REGISTRY.get(key))
                       .orElseThrow(() -> new RuntimeException("Unknown animal: " + key))
                       .get();
    }
}

/* ===========================
   Execution Orchestration Layer
   =========================== */

class AnimalExecutionPipeline {

    private final List<Animal> animals;

    private AnimalExecutionPipeline(List<Animal> animals) {
        this.animals = animals;
    }

    public static AnimalExecutionPipeline build(List<String> descriptors) {
        List<Animal> resolvedAnimals = new ArrayList<>();
        for (String descriptor : descriptors) {
            resolvedAnimals.add(AnimalFactory.create(descriptor));
        }
        return new AnimalExecutionPipeline(resolvedAnimals);
    }

    public void execute() {
        animals.stream()
               .peek(Objects::requireNonNull)
               .forEach(Animal::animalSound);
    }
}

/* ===========================
   Main Class (Finally)
   =========================== */

public class Main {

    public static void main(String[] args) {

        List<String> configurationDrivenAnimalList =
                Arrays.asList("animal", "pig", "dog", "cat");

        AnimalExecutionPipeline
                .build(configurationDrivenAnimalList)
                .execute();
    }
}
