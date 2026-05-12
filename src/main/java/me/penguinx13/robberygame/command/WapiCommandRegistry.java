package me.penguinx13.robberygame.command;

import java.util.List;

import me.penguinx13.wapi.commands.core.pipeline.ArgumentParsingStage;
import me.penguinx13.wapi.commands.core.pipeline.AuthorizationStage;
import me.penguinx13.wapi.commands.core.pipeline.CommandPipeline;
import me.penguinx13.wapi.commands.core.pipeline.InvocationStage;
import me.penguinx13.wapi.commands.core.pipeline.PostProcessingStage;
import me.penguinx13.wapi.commands.core.pipeline.RoutingStage;
import me.penguinx13.wapi.commands.core.pipeline.ValidationStage;
import me.penguinx13.wapi.commands.core.registry.CommandRegistrationService;
import me.penguinx13.wapi.commands.core.resolver.DefaultResolvers;
import me.penguinx13.wapi.commands.core.resolver.ResolverRegistry;
import me.penguinx13.wapi.commands.core.runtime.CommandRuntime;
import me.penguinx13.wapi.commands.core.runtime.NoopMetricsSink;
import me.penguinx13.wapi.commands.core.validation.ValidationService;
import me.penguinx13.wapi.commands.paper.error.DefaultErrorPresenter;
import me.penguinx13.wapi.commands.paper.platform.PaperCommandBinder;
import me.penguinx13.wapi.commands.paper.platform.PaperLogger;
import me.penguinx13.wapi.commands.paper.platform.PaperPlatformBridge;
import me.penguinx13.wapi.commands.paper.platform.PaperPlayerResolver;
import me.penguinx13.wapi.commands.paper.platform.PaperScheduler;
import org.bukkit.plugin.java.JavaPlugin;

public final class WapiCommandRegistry {

    private final JavaPlugin plugin;
    private final PaperScheduler scheduler;
    private final PaperPlatformBridge bridge;
    private final CommandRegistrationService registrationService;

    public WapiCommandRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        this.scheduler = new PaperScheduler(plugin);
        this.bridge = new PaperPlatformBridge(scheduler);
        this.registrationService = new CommandRegistrationService();
    }

    public void register(Object command) {
        registrationService.register(command);
    }

    public void bind() {
        ResolverRegistry resolverRegistry = new ResolverRegistry();
        DefaultResolvers.registerDefaults(resolverRegistry);
        resolverRegistry.register(new PaperPlayerResolver());

        CommandRuntime runtime = new CommandRuntime(
                registrationService.buildTree(),
                new CommandPipeline(List.of(
                        new RoutingStage(),
                        new ArgumentParsingStage(),
                        new ValidationStage(),
                        new AuthorizationStage(),
                        new InvocationStage(),
                        new PostProcessingStage()
                )),
                resolverRegistry,
                new ValidationService(),
                new DefaultErrorPresenter(new PaperLogger(plugin.getLogger())),
                List.of(),
                bridge,
                new NoopMetricsSink()
        );

        new PaperCommandBinder(plugin, bridge).bind(runtime);
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
