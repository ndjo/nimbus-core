/**
 * 
 */
package com.anthem.oss.nimbus.core.domain.model.state.internal;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

import com.anthem.oss.nimbus.core.FrameworkRuntimeException;
import com.anthem.oss.nimbus.core.domain.command.Command;
import com.anthem.oss.nimbus.core.domain.definition.MapsTo;
import com.anthem.oss.nimbus.core.domain.definition.MapsTo.Path;
import com.anthem.oss.nimbus.core.domain.definition.Repo;
import com.anthem.oss.nimbus.core.domain.definition.Repo.Database;
import com.anthem.oss.nimbus.core.domain.model.config.ModelConfig;
import com.anthem.oss.nimbus.core.domain.model.config.ParamConfig;
import com.anthem.oss.nimbus.core.domain.model.config.ParamType;
import com.anthem.oss.nimbus.core.domain.model.config.internal.DefaultModelConfig;
import com.anthem.oss.nimbus.core.domain.model.config.internal.DefaultParamConfig;
import com.anthem.oss.nimbus.core.domain.model.config.internal.MappedDefaultParamConfig;
import com.anthem.oss.nimbus.core.domain.model.state.EntityState.ExecutionModel;
import com.anthem.oss.nimbus.core.domain.model.state.EntityState.Param;
import com.anthem.oss.nimbus.core.domain.model.state.ExecutionRuntime;
import com.anthem.oss.nimbus.core.domain.model.state.Notification;
import com.anthem.oss.nimbus.core.domain.model.state.StateBuilderContext;
import com.anthem.oss.nimbus.core.domain.model.state.StateType;
import com.anthem.oss.nimbus.core.entity.AbstractEntity;
import com.anthem.oss.nimbus.core.entity.process.ProcessFlow;
import com.anthem.oss.nimbus.core.util.LockTemplate;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @author Soham Chakravarti
 *
 */

@Repo(Database.rep_mongodb)
@Getter @Setter
public class ExecutionEntity<V, C> extends AbstractEntity.IdString implements Serializable {

	private static final long serialVersionUID = 1L;
	
	//==private CommandElement key;
	
	private C c;

	@Path("/c") private V v;
	
	private ProcessFlow f;
	
	private Map<String, RuntimeEntity> paramRuntimes;
	
	public C getCore() {return getC();}
	public void setCore(C c) {setC(c);}
	
	public V getView() {return getV();}
	public void setView(V v) {setV(v);}
	
	public ProcessFlow getFlow() {return getF();}
	public void setFlow(ProcessFlow f) {setF(f);}
	
	private ExecutionEntity<V, C> _this() {
		return this;
	}
	
	@Getter @Setter @RequiredArgsConstructor
	public static class ExConfig<V, C> {
		final private ModelConfig<C> core;
		final private ModelConfig<V> view;
		final private ModelConfig<ProcessFlow> flow;
	}
	
	@Getter @Setter 
	public class ExParamConfig extends DefaultParamConfig<ExecutionEntity<V, C>> {
		private static final long serialVersionUID = 1L;
		
		final private ModelConfig<ExecutionEntity<V, C>> rootParent;
		
		public ExParamConfig(ExConfig<V, C> exConfig) {
			super("root");
			this.rootParent = new ExModelConfig(exConfig); 
			
			ParamType.Nested<ExecutionEntity<V, C>> pType = new ParamType.Nested<>(_this().getClass().getSimpleName(), _this().getClass());
			pType.setModel(getRootParent());
			this.setType(pType);
		}
	}
	
	@Getter @Setter
	public class ExModelConfig extends DefaultModelConfig<ExecutionEntity<V, C>> {
		private static final long serialVersionUID = 1L;
		
		final private ParamConfig<C> coreParam;
		final private ParamConfig<V> viewParam;
		final private ParamConfig<ProcessFlow> flowParam;
		
		@SuppressWarnings("unchecked")
		public ExModelConfig(ExConfig<V, C> exConfig) {
			super((Class<ExecutionEntity<V, C>>)_this().getClass());
			
			//TODO: change to client default 
			setRepo(AnnotationUtils.findAnnotation(ExecutionEntity.class, Repo.class));
			
			Objects.requireNonNull(exConfig.getCore(), "Core ModelConfig must be provided.");
			this.coreParam = attachParams("c", exConfig.getCore());
			
			this.viewParam = exConfig.getView()!=null ? attachParams("v", exConfig.getView()) : null;
			this.flowParam = exConfig.getFlow()!=null ? attachParams("f", exConfig.getFlow()) : null;
		}
		
		private <T> ParamConfig<T> attachParams(String pCode, ModelConfig<T> modelConfig) {
			DefaultParamConfig<T> pConfig = createParam(pCode);
			Class<T> pClass = modelConfig.getReferredClass();
			
			ParamType.Nested<T> pType = new ParamType.Nested<>(ClassUtils.getShortName(pClass), pClass);
			pConfig.setType(pType);
			pType.setModel(modelConfig);
			
			templateParams().add(pConfig);
			return pConfig;
		}
		
		private <T> DefaultParamConfig<T> createParam(String pCode) {
			Field f = FieldUtils.getDeclaredField(ExecutionEntity.class, pCode, true);
			MapsTo.Path path = AnnotationUtils.findAnnotation(f, MapsTo.Path.class);
			
			DefaultParamConfig<T> pConfig = path==null ? new DefaultParamConfig<>(pCode) : new MappedDefaultParamConfig<>(pCode, this, findParamByPath(path.value()), path);
			return pConfig;
		} 
	}
	
	@Setter 
	public class ExParam extends DefaultParamState<ExecutionEntity<V, C>> {
		private static final long serialVersionUID = 1L;
		
		final private ExecutionModel<ExecutionEntity<V, C>> rootModel;
		
		public ExParam(Command cmd, StateBuilderContext provider, ExConfig<V, C> exConfig) {
			super(null, new ExParamConfig(exConfig), provider);
			ExParamConfig pConfig = ((ExParamConfig)getConfig());
			
			this.rootModel = new ExModel(cmd, this, pConfig.getRootParent(), provider);
			this.setType(new StateType.Nested<>(getConfig().getType().findIfNested(), getRootExecution()));
		}

		public ExParam(Command cmd, StateBuilderContext provider, ParamConfig<ExecutionEntity<V, C>> rootParamConfig, ExecutionModel<ExecutionEntity<V, C>> rootModel) {
			super(null, rootParamConfig, provider);
			this.rootModel = rootModel;
		}
		
		@Override
		public boolean isRoot() {
			return true;
		}
		
		@Override
		public ExecutionModel<ExecutionEntity<V, C>> getRootExecution() {
			return rootModel;
		}
		
		@Override
		public void fireRules() {
			findParamByPath("/c").fireRules();
			Optional.ofNullable(findParamByPath("/v")).ifPresent(v->v.fireRules());
		}
	}
	
	@Getter @Setter
	public class ExModel extends DefaultModelState<ExecutionEntity<V, C>> implements ExecutionModel<ExecutionEntity<V, C>> {
		private static final long serialVersionUID = 1L;
		
		final private Command command;
		
		private String currentPage; //TODO change to state
		
		@JsonIgnore
		final private ExecutionRuntime executionRuntime;
		
		public ExModel(Command command, ExParam associatedParam, ModelConfig<ExecutionEntity<V, C>> modelConfig, StateBuilderContext provider) {
			this(command, associatedParam, modelConfig, provider, new InternalExecutionRuntime(command));
		}
		
		public ExModel(Command command, ExParam associatedParam, ModelConfig<ExecutionEntity<V, C>> modelConfig, StateBuilderContext provider, ExecutionRuntime executionRuntime) {
			super(associatedParam, modelConfig, provider);
			this.command = command;
			this.executionRuntime = executionRuntime;
		}
		
		@Override
		protected void initInternal() {
			getExecutionRuntime().start();
		}
		
		@Override
		public ExecutionEntity<V, C> instantiateOrGet() {
			return _this();
		}
		
		@Override
		public ExModelConfig getConfig() {
			return (ExModelConfig)super.getConfig();
		}
		
		@Override
		public ExParam getAssociatedParam() {
			return (ExParam)super.getAssociatedParam();
		}
		
		@JsonIgnore @Override
		public ExModel getRootExecution() {
			return this;
		}
		
		@Override
		public ExecutionEntity<V, C> getState() {
			return _this();
		}
		
		@Override
		protected void finalize() throws Throwable {
			getExecutionRuntime().stop();
			
			super.finalize();
		}
	}
	
	@Getter
	public class InternalExecutionRuntime implements ExecutionRuntime {

		final private Command command;
		final private BlockingQueue<Notification<?>> notificationQueue;
		
		private String lockId;
		final private LockTemplate lock = new LockTemplate();
		
		final private LockTemplate notificationLock = new LockTemplate();
		final private Condition notComplete = notificationLock.getLock().newCondition();
		
		public InternalExecutionRuntime(Command cmd) {
			this.command = cmd;
			this.notificationQueue = new LinkedBlockingQueue<>();
		}
		
		@Override
		public void start() { }
		
		@Override
		public void stop() { }

		@Override
		public String tryLock() {
			if(isLocked()) return null;
			
			return getLock().execute(()-> {
				this.lockId = UUID.randomUUID().toString();
				return getLockId();
			});
		}

		@Override
		public boolean isLocked() {
			return (getLockId()!=null);
		}
		
		@Override
		public boolean isLocked(String lockId) {
			if(!isLocked()) return false;
			
			return getLockId().equals(lockId);
		}
		
		@Override
		public boolean tryUnlock(String lockId) {
			if(!isLocked(lockId)) return true;
			
			return getLock().execute(()-> {
				if(isLocked(lockId)) {
					this.lockId = null;
					return true;
				}
				
				return false;
			});
			
		}
		
		@Override
		public void notifySubscribers(Notification<Object> event) {
			try {
				getNotificationQueue().put(event);
			} catch (InterruptedException ex) {
				throw new FrameworkRuntimeException("Failed to place notification event on queue. event: "+event, ex);
			}	
		}
		
		@Override
		public void awaitCompletion() {
			// create new single thread using executor service
			ExecutorService e = createExecutorService(getCommand());
			e.execute(new InternalNotificationEventDelegator(getNotificationQueue(), getNotificationLock(), getNotComplete()));

			// shutdown
			e.shutdown();
			
			// wait on lock condition for completion of all tasks
			// TODO: this is overkill, can be simplified by waiting on ExecutorServive.awaitTermination() but kept for future enhancement
			getNotificationLock().execute(()-> {
				try {
					while(getNotificationQueue().size()!=0)
						notComplete.await();
					
				} catch (InterruptedException ex) {
					throw new FrameworkRuntimeException("Failed while waiting for notification event processing to complete. queue: "+notificationQueue, ex);
				}
			});
		}
		
		private ExecutorService createExecutorService(Command cmd) {
			return Executors.newFixedThreadPool(1, new ThreadFactory() {
				
				final private AtomicInteger counter = new AtomicInteger();
				
				@Override
				public Thread newThread(Runnable r) {
					return new Thread(r, "ExecState "+counter.incrementAndGet()+": "+cmd.getRootDomainElement().getUri());
				}
			});
		}
	} 
	
	@Getter @RequiredArgsConstructor
	public class InternalNotificationEventDelegator implements Runnable {
		final private BlockingQueue<Notification<?>> notificationQueue;
		final private LockTemplate notificationLock;
		final private Condition notComplete;
		
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			try {
				while(true) {
					Notification<Object> event = (Notification<Object>)notificationQueue.take();
					Param<Object> source =  event.getSource();
					
					// create new list to avoid concurrent modification of subscriber list as part of event handling
					new ArrayList<>(source.getEventSubscribers())
						.stream()
						.forEach(subscribedParam->subscribedParam.handleNotification(event));
					
					getNotificationLock().execute(()-> {
						if(notificationQueue.size()==0)
							notComplete.signal();
					});
				}
				
			} catch (InterruptedException ex) {
				throw new FrameworkRuntimeException("Failed to take event form queue: "+notificationQueue, ex);
			}
		}
	}
}