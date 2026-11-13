import logging

# Our own loggers at INFO, for refusals (auth-layer AL15); uvicorn sets up only its own.
logging.basicConfig(format="%(levelname)s %(name)s: %(message)s")
logging.getLogger("echocare").setLevel(logging.INFO)

# Everything under /api, docs included, so the API and the dashboard can share one domain.
app = FastAPI(
    title="EchoCare",
    docs_url="/api/docs",
    redoc_url="/api/redoc",
    openapi_url="/api/openapi.json",
    lifespan=lifespan,
)
# Browser requests only from the dashboard's own address (01 §4, channel 3).
app.add_middleware(
    CORSMiddleware,
    allow_origins=cors_origins(settings),
    allow_methods=["*"],
    allow_headers=["*"],
)
api = APIRouter(prefix="/api")


# after every route is declared: include_router copies the routes it has at this point
app.include_router(api)

__all__ = ["app"]
