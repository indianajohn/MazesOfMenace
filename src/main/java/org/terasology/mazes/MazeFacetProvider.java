package org.terasology.mazes;

import org.terasology.customOreGen.PDist;
import org.terasology.customOreGen.Structure;
import org.terasology.customOreGen.StructureDefinition;
import org.terasology.customOreGen.VeinsStructureDefinition;
import org.terasology.entitySystem.Component;
import org.terasology.math.geom.Vector3i;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.world.generation.ConfigurableFacetProvider;
import org.terasology.world.generation.FacetProviderPlugin;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generator.plugin.RegisterPlugin;

import java.util.Collection;

@RegisterPlugin
@Produces(MazeFacet.class)
public class MazeFacetProvider implements ConfigurableFacetProvider, FacetProviderPlugin {
    private long seed;
    private MazeFacetProviderConfiguration configuration = new MazeFacetProviderConfiguration();

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
    }

    @Override
    public void process(GeneratingRegion region) {
        MazeFacet facet = new MazeFacet(region.getRegion(), region.getBorderForFacet(MazeFacet.class));
        MazeGenerator maze_generator = new MazeGenerator(new MazeInfo(seed));
        if (maze_generator.intersectsWithRegion(region.getRegion()))
        {
            for (Vector3i position : facet.getWorldRegion())
            {
                if (maze_generator.containsWorldPosition(position))
                {
                    facet.setWorld(position,true);
                }
            }
        }
        region.setRegionFacet(MazeFacet.class, facet);
    }


    @Override
    public String getConfigurationName() {
        return "Mazes";
    }

    @Override
    public Component getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Component configuration) {
        this.configuration = (MazeFacetProviderConfiguration) configuration;
    }

    private static class MazeFacetProviderConfiguration implements Component {
        @Range(min = 0, max = 1f, increment = 0.01f, precision = 2, description = "Maze Frequency")
        public float frequency = 0.1f;
        @Range(min = 0, max = 25f, increment = 1f, precision = 0, description = "Maze Radius")
        public float mazeRadius = 8f;
        @Range(min = 0, max = 10f, increment = 1f, precision = 0, description = "Tunnel Radius")
        public float tunnelRadius = 4f;
    }
}
