package net.rizon.moo.injectors.logger;

import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import java.lang.reflect.Field;
import org.slf4j.Logger;

public class LogTypeListener implements TypeListener
{
	@Override
	public <T> void hear(TypeLiteral<T> typeLiteral, TypeEncounter<T> typeEncounter)
	{
		Class<?> clazz = typeLiteral.getRawType();
		while (clazz != null)
		{
			for (Field field : clazz.getDeclaredFields())
			{
				if (field.getType() == Logger.class && field.isAnnotationPresent(Inject.class))
				{
					typeEncounter.register(new LogInjector<T>(field));
				}
			}
			clazz = clazz.getSuperclass();
		}
	}
}

