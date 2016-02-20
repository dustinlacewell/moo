package net.rizon.moo.injectors.logger;

import com.google.inject.MembersInjector;
import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogInjector<T> implements MembersInjector<T>
{
	private final Field field;
	
	LogInjector(Field field)
	{
		this.field = field;	
	}
	
	@Override
	public void injectMembers(T instance)
	{
		Logger logger = LoggerFactory.getLogger(field.getDeclaringClass());
		
		boolean accessable = field.isAccessible();
		field.setAccessible(true);
		try
		{
			field.set(instance, logger);
		}
		catch (IllegalAccessException ex)
		{
			throw new RuntimeException(ex);
		}
		finally
		{
			field.setAccessible(accessable);
		}
	}

}
